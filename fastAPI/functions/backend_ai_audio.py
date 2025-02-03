import os
import openai
from openai import OpenAI
import argparse
import time
import json
import subprocess
import re
import base64
import torch
from transformers import AutoTokenizer, AutoModelForSequenceClassification
from faster_whisper import WhisperModel
from faster_whisper.transcribe import BatchedInferencePipeline
from functions.stt import run as stt

os.environ['KMP_DUPLICATE_LIB_OK'] = 'True'

# Define API key file path
API_KEY_PATH = "my_openai_api.txt"

def load_key():
    with open(API_KEY_PATH, 'r') as f:
        openAI_key = f.readline().strip()
    return openAI_key

openai.api_key = load_key()
os.environ['OPENAI_API_KEY'] = openai.api_key

# API 기반 Whisper
def audio_to_text(audio_file):
    client = OpenAI()
    # STT using Whisper (OpenAI)
    transcript = client.audio.transcriptions.create(
        file=audio_file,
        model="whisper-1",
        language="ko",
        response_format="text"
    )
    return transcript

# =============================================================================================
# =============================================================================================
# =============================================================================================

def call_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('--do_compress', type=int, default=1, choices=[0, 1])
    parser.add_argument('--check_time', type=int, default=0, choices=[0, 1])
    args = parser.parse_args()

    if args.do_compress == 0: args.do_compress = True
    else: args.do_compress = False

    if args.check_time == 0: args.check_time = True
    else: args.check_time = False
    return args

# Full transcript -> sentence-wise scripts
def sep_sentences(full_transcript):
    sentences = []
    temp_sentence = ""

    for text in full_transcript:
        split_text = re.split(r'(?<=[.!?]) +', text)

        for sentence in split_text:
            # 해당 문장이 문장 구분자로 끝나는 경우
            if re.search(r'[.!?]$', sentence):
                # 임시로 저장된 문장 구분자가 없는 문장이 앞에 있던 경우
                if temp_sentence:
                    temp_sentence += " " + sentence
                    sentences.append(temp_sentence.strip())
                    temp_sentence = ""
                # 임시로 저장된 문장 구분자가 직전에 없는 경우
                else:
                    sentences.append(sentence.strip())
            # 해당 문장이 문장 구분자로 끝나지 않는 경우; 임시로 저장
            else:
                # 해당 문장이 임시 문장 공간에 초기화 이후 처음으로 온 문장이 아닌 경우
                if temp_sentence:
                    temp_sentence += " " + sentence
                # 해당 문장이 초기화 이후 처음으로 임시 문장으로 저장된 경우
                else:
                    temp_sentence += sentence
        
    # 마지막 문장이 문장 구분자로 끝나지 않는 경우 다음 줄로 임시 문장 이관
    if temp_sentence:
        sentences.append(temp_sentence.strip())
        temp_sentence = ""

    return sentences

def save_log(sentences, file_path):
    if file_path.endswith('txt'):
        with open(file_path, 'w', encoding='utf-8') as file:
            for sentence in sentences:
                file.write(sentence + "\n")
    elif file_path.endswith('json'):
        with open(file_path, 'w', encoding='utf-8') as file:
            json.dump(sentences, file, ensure_ascii=False, indent=4)

# =============================================================================================
# =============================================================================================
# =============================================================================================
# =============================================================================================
# =============================================================================================
# =============================================================================================

def text_summ_topicwise(input_text, gptModel):
    client = OpenAI()

    system_role = '''당신은 회의 중 안건들을 요약하여 작성해주는 서기입니다. 안건들을 주제별로 묶어서 간결하고 명료하게 두세 문장으로 요약해주세요. 
    응답은 다음의 예시와 같은 형식으로 주제별로 보내주세요.
    {"topic": \"주제\",
    "content": \"
    ● 주제1 
    -  의견1
    -  의견2 
    -  의견3 
    ● 주제 2
    -  의견1 
    -  의견2 \"}
    '''

    response = client.chat.completions.create(
        model=gptModel,
        messages=[
            {
                "role": "system",
                "content": system_role
            },
            {
                "role": "user",
                "content": input_text
            }
        ]
    )

    answer = response.choices[0].message.content
    answer_ = answer.split(': \"')[-1].split('\"')[0]
    return answer

def text_summ_posneg(input_text, gptModel):
    client = OpenAI()

    system_role = '''당신은 회의 중 안건들을 요약하여 작성해주는 서기입니다. 아래 텍스트를 읽고 "긍정 의견", "부정 의견", "중립 의견", "결론" 네 가지 카테고리로 요약해주세요.
    각 카테고리는 비어있을 수도 있습니다.
    1. **긍정 의견**: 제안된 아이디어에 대해 긍정적인 의견을 요약합니다.
    2. **부정 의견**: 제안된 아이디어에 대해 부정적인 의견이나 문제점을 요약합니다.
    3. **중립 의견**: 제안된 아이디어와 관련된 단순 정보 등을 요약합니다.
    4. **결론**: 논의된 내용을 종합하여 결론을 제시합니다.

    응답은 다음의 예시의 형식을 참고하여 보내주세요.

    {"content": \"
    ● 긍정 의견 
    -  의견1
    -  의견2 
    -  의견3 
    ● 부정 의견
    -  의견1 
    -  의견2 
    ● 중립 의견
    -  의견1
    ● 결론
    -  의견1
    -  의견2 \"}
    '''

    response = client.chat.completions.create(
        model=gptModel,
        messages=[
            {
                "role": "system",
                "content": system_role
            },
            {
                "role": "user",
                "content": input_text
            }
        ]
    )

    answer = response.choices[0].message.content
    answer_ = answer.split(': \"')[-1].split('\"')[0]
    return answer

def text_summ_total(input_text, gptModel):
    client = OpenAI()

    system_role = '''당신은 회의 중 안건들을 요약하여 작성해주는 서기입니다. 입력된 텍스트의 전체 내용을 읽기 쉽게 간결하고 명확하게 요약해주세요.
    응답은 다음의 형식을 지켜주세요.
    {"summary": \"전체 회의 내용 요약\"}
    '''

    response = client.chat.completions.create(
        model=gptModel,
        messages=[
            {
                "role": "system",
                "content": system_role
            },
            {
                "role": "user",
                "content": input_text
            }
        ]
    )

    answer = response.choices[0].message.content
    answer_ = answer.split(': \"')[-1].split('\"')[0]
    return answer


def encode_image(image_path):
    with open(image_path, "rb") as image_file:
        return base64.b64encode(image_file.read()).decode('utf-8')

def image_summ(image_path, gptModel):
    encoded_image = encode_image(image_path)
    client = OpenAI()

    system_role = '''당신은 발표의 신입니다. 발표자료를 보면 이해한 후, 간단하고 명확하게 이해할 수 있도록 자료를 텍스트로 재구성해줍니다. 이해하기 쉽게 간단하고 명확하게 요약해주세요.
    요약 형식은 다음을 참고해서 만들어주세요.

    -  발표자료 핵심 키워드1: 설명
    -  발표자료 핵심 키워드2: 설명
    -  발표자료 핵심 키워드3: 설명
    '''

    prompt = '이 발표자료를 이해하기 쉽게 요약해줘'

    response = client.chat.completions.create(
        model=gptModel,
        messages=[
            {
                "role": "system",
                "content": system_role
            },
            {
                "role": "user",
                "content": [
                    {
                        "type": "text", "text": prompt
                    },
                    {
                        "type": "image_url",
                        "image_url": {"url": f"data:image/png;base64, {encoded_image}"}
                    },
                ],
            }
        ],
    )
    return response.choices[0].message.content

# =============================================================================================
# =============================================================================================
# =============================================================================================
# =============================================================================================
# =============================================================================================
# =============================================================================================

def summarize_audio(audio_file, do_compress=False):
    full_transcript = stt(audio_file, do_compress)
    
    sentences = sep_sentences(full_transcript)
    
    # save_log(sentences, f"data/{re.sub("wav", "txt", audio_file)}")
    save_log(sentences, f"{re.sub("wav", "txt", audio_file)}")
             
    gptModel = "gpt-4o"
    summ_topicwise = text_summ_topicwise(" ".join(sentences), gptModel)
    summ_posneg = text_summ_posneg(" ".join(sentences), gptModel)
    
    print("====================================")
    print("topicwise summary ==================")
    print("====================================")
    print(summ_topicwise)
    
    print("====================================")
    print("positive negative summary ==========")
    print("====================================")
    print(summ_posneg)
    
    return summ_topicwise, summ_posneg

if __name__ == '__main__':
    args = call_args()
    full_transcript = stt('data/audio1-3.m4a', args.do_compress)

    t0 = time.time()
    sentences = sep_sentences(full_transcript)
    t1 = time.time()
    if args.check_time: print('seperating time ', t1-t0)
    save_log(sentences, 'data/audio1_3_log.txt')

    gptModel = "gpt-4o"
    summ = text_summ_topicwise(' '.join(sentences), gptModel)
    print(summ.split(': \"')[-1].split('\"')[0])
    summ = text_summ_posneg(' '.join(sentences), gptModel)
    print(summ.split(': \"')[-1].split('\"')[0])