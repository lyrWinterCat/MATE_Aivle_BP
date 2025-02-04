import os
import openai
from openai import OpenAI
import argparse
import time
import json
import subprocess
import pickle
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
    1. **긍정 의견**: 제안된 아이디어에 대해 긍정적인 의견을 요약합니다. 긍정적인 의견이 딱히 없다면 없음으로 표기해도 좋습니다.
    2. **부정 의견**: 제안된 아이디어에 대해 부정적인 의견이나 문제점을 요약합니다. 부정적인 의견이 딱히 없다면 없음으로 표기해도 좋습니다.
    3. **중립 의견**: 제안된 아이디어와 관련된 단순 정보 등을 요약합니다. 중립적인 의견이 딱히 없다면 없음으로 표기해도 좋습니다.
    4. **결론**: 논의된 내용을 종합하여 결론을 제시합니다. 간결하고 명료하게 정리해주세요.

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
    # answer_ = answer.split(': \"')[-1].split('\"')[0]
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
    # answer_ = answer.split(': \"')[-1].split('\"')[0]
    return answer

def text_summ_todo(input_text, gptModel):
    client = OpenAI()

    system_role = '''당신은 회의 중 안건들을 요약하여 작성해주는 서기입니다. 입력된 회의 안건 텍스트를 주제별로 묶어서 회의중에 얘기된 현재 상황과, 이에 관해 회의 중에 얘기된 앞으로 해야 할 일을 정리해주세요.
    현재 상황 또는 현재 상황과 관련해서 앞으로 해야 할 일이 입력된 텍스트에서 제시되지 않았다면 생략하고 해당 주제의 현재 상황만 작성해주세요. 
    누락되는 내용이 없되, 읽기 좋게 간결하고 명료하게 작성해주세요. 응답은 다음 형식을 참고해주세요.
    {"topic": \"주제\",
    "content": \"
    ● 주제1 
    -  현황: 주제1의 현재 상황 관련 안건 요약
    -  ToDo: 주제1에 관련하여 회의에서 제시된 앞으로 해야 할 일에 관한 안건 요약 
    ● 주제 2
    -  현황: 주제2의 현재 상황 관련 안건 요약 \"}

    다시 한번 말하지만, 앞으로 해야 할 일이 회의 중에 제시되지 않았다면 ToDo를 생략해주세요. 없는 이야기를 함부로 만들지 말아주세요.
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
    # answer_ = answer.split(': \"')[-1].split('\"')[0]
    return answer

def text_summs2summ_topicwise(input_texts, gptModel):
    client = OpenAI()
    total_summ = ' [sep]'.join(input_texts)
    system_role = '''당신은 회의 중 나온 안건 요약본을 정리해주는 서기입니다. 복수의 요약된 회의 내용들을 입력으로 받아 안건들을 중복되는 내용 없이 정리합니다.
    각 요약된 텍스트는 가운데에 [sep]으로 구분됩니다. 응답은 다음의 예시와 같은 형식을 참고해서 보내주세요.
    {"topic": \"주제\",
    "content": \"
    ● 주제1 
    -  의견1
    -  의견2 
    -  의견3 
    ● 주제2
    -  의견1 
    -  의견2\"}
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
                "content": total_summ
            }
        ]
    )

    answer = response.choices[0].message.content
    # answer_ = answer.split(': \"')[-1].split('\"')[0]
    return answer

def text_summs2summ_posneg(input_texts, gptModel):
    client = OpenAI()
    total_summ = ' [sep]'.join(input_texts)
    system_role = '''당신은 회의 중 안건 요약본을 정리해주는 서기입니다. "긍정 의견", "부정 의견", "중립 의견", "결론" 네 가지 카테고리로 요약된 복수의 요약된 회의 내용들을 입력으로 받아 안건들을 중복되는 내용 없이 정리합니다.
    각 요약된 텍스트는 가운데에 [sep]으로 구분됩니다. 아래 텍스트를 읽고 "긍정 의견", "부정 의견", "중립 의견", "결론" 네 가지 카테고리로 정리해주세요.
    각 카테고리는 비어있을 수도 있습니다.
    1. **긍정 의견**: 제안된 아이디어에 대해 긍정적인 의견을 요약합니다. 긍정적인 의견이 딱히 없다면 없음으로 표기해도 좋습니다.
    2. **부정 의견**: 제안된 아이디어에 대해 부정적인 의견이나 문제점을 요약합니다. 부정적인 의견이 딱히 없다면 없음으로 표기해도 좋습니다.
    3. **중립 의견**: 제안된 아이디어와 관련된 단순 정보 등을 요약합니다. 중립적인 의견이 딱히 없다면 없음으로 표기해도 좋습니다.
    4. **결론**: 논의된 내용을 종합하여 결론을 제시합니다. 간결하고 명료하게 정리해주세요.

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
                "content": total_summ
            }
        ]
    )

    answer = response.choices[0].message.content
    # answer_ = answer.split(': \"')[-1].split('\"')[0]
    return answer

def text_summs2summ_todo(input_texts, gptModel):
    client = OpenAI()

    total_summ = ' [sep]'.join(input_texts)
    system_role = '''당신은 회의 중 안건 요약본을 정리해주는 서기입니다. 복수의 요약된 회의 안건들을 텍스트로 입력 내의 주제별 현황과 ToDo를 중복되는 내용 없이 정리합니다.
    각 요약된 텍스트는 가운데에 [sep]으로 구분됩니다. 
    현재 상황 또는 현재 상황과 관련해서 앞으로 해야 할 일이 입력된 텍스트에서 제시되지 않았다면 생략하고 해당 주제의 현재 상황만 작성해주세요. 응답은 다음 형식을 참고해주세요.
    {"topic": \"주제\",
    "content": \"
    ● 주제1 
    -  현황: 주제1의 현재 상황 관련 안건 요약
    -  ToDo: 주제1에 관련하여 회의에서 제시된 앞으로 해야 할 일에 관한 안건 요약 
    ● 주제 2
    -  현황: 주제2의 현재 상황 관련 안건 요약 \"}

    다시 한번 말하지만, 앞으로 해야 할 일이 회의 중에 제시되지 않았다면 ToDo를 생략해주세요. 없는 이야기를 함부로 만들지 말아주세요.
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
                "content": total_summ
            }
        ]
    )

    answer = response.choices[0].message.content
    # answer_ = answer.split(': \"')[-1].split('\"')[0]
    return answer

def text_summs2summ_total(input_texts, gptModel):
    client = OpenAI()

    total_summ = ' [sep]'.join(input_texts)
    system_role = '''당신은 회의 중 안건 요약본을 정리해주는 서기입니다. 입력된 텍스트의 전체 내용을 읽기 쉽되 누락되는 내용이 없도록 정리해주세요.
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
                "content": total_summ
            }
        ]
    )

    answer = response.choices[0].message.content
    # answer_ = answer.split(': \"')[-1].split('\"')[0]
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

def read_txt(fp):
    sentences = []
    with open(fp, mode='r', encoding='utf-8') as file:
        sentences = file.readlines()
        sentences = [sentence.strip() for sentence in sentences]
    return sentences

# 분기별 (기간별) 요약을 하나로 합치기
def encapsule_summs(summsTopic, summsPN, summsTD, summsTotal):
    summsLog = []
    summsLog.append('[주제별 요약]\n')

# 하나의 요약본을 분기별 요약으로 나누기
def load_tmp(meeting_name):
    if not os.path.exists(f'{meeting_name}/tmp/summsTopic.p'):
        summsTopic = []
    else:
        with open(f'{meeting_name}/tmp/summsTopic.p', 'rb') as f:
            summsTopic = pickle.load(f)

    if not os.path.exists(f'{meeting_name}/tmp/summsPN.p'):
        summsPN = []
    else:
        with open(f'{meeting_name}/tmp/summsPN.p', 'rb') as f:
            summsPN = pickle.load(f)

    if not os.path.exists(f'{meeting_name}/tmp/summsTD.p'):
        summsTD = []
    else:
        with open(f'{meeting_name}/tmp/summsTD.p', 'rb') as f:
            summsTD = pickle.load(f)

    if not os.path.exists(f'{meeting_name}/tmp/summsTotal.p'):
        summsTotal = []
    else:
        with open(f'{meeting_name}/tmp/summsTotal.p', 'rb') as f:
            summsTotal = pickle.load(f)

    return summsTopic, summsPN, summsTD, summsTotal

def save_tmp(summsTopic, summsPN, summsTD, summsTotal, meeting_name):
    os.makedirs(f'{meeting_name}/tmp', exist_ok=True)

    for target in ['summsTopic', 'summsPN', 'summsTD', 'summsTotal']:
        path = f'{meeting_name}/tmp/{target}.p'
        if os.path.exists(path):
            os.remove(path)
        if target == 'summsTopic':
            with open(path, 'wb') as f: pickle.dump(summsTopic, f)
        elif target == 'summsPN':
            with open(path, 'wb') as f: pickle.dump(summsPN, f)
        elif target == 'summsTD':
            with open(path, 'wb') as f: pickle.dump(summsTD, f)
        elif target == 'summsTotal':
            with open(path, 'wb') as f: pickle.dump(summsTotal, f)
        else:
            raise NotImplementedError('Wrong target')

def del_tmp(meeting_name):
    for target in ['summsTopic', 'summsPN', 'summsTD', 'summsTotal']:
        path = f'{meeting_name}/tmp/{target}.p'
        if os.path.exists(path):
            os.remove(path)

def summarize_audio(meeting_name, do_compress=False):
    doneList_fp = f"{meeting_name}/doneList.txt"
    
    if not os.path.exists(doneList_fp):
        with open(doneList_fp, "w", encoding="utf-8") as f: pass
        doneList = []
    else:
        doneList = read_txt(doneList_fp)
        
    summsTopic, summsPN, summsTD, summsTotal = load_tmp(meeting_name)
    
    audio_files = [audio_file for audio_file in os.listdir(meeting_name) if audio_file.endswith("wav")]
    
    for fp in audio_files:
        if fp in doneList: continue
        else:
            file_name, _ = os.path.splitext(fp)
            print('File name: ', file_name)
            
            log_fp = f'{meeting_name}/{file_name}_log.txt'
            if not os.path.exists(log_fp):
                full_transcript = stt(f"{meeting_name}/{fp}", do_compress)
                
                sentences = sep_sentences(full_transcript)
                
                save_log(sentences, log_fp)
                
            sentences = read_txt(log_fp)
                
            gptModel = "gpt-4o"
            summ = text_summ_topicwise(' '.join(sentences), gptModel)
            summsTopic.append(summ)

            summ = text_summ_posneg(' '.join(sentences), gptModel)
            summsPN.append(summ)

            summ = text_summ_todo(' '.join(sentences), gptModel)
            summsTD.append(summ)

            summ = text_summ_total(' '.join(sentences), gptModel)
            summsTotal.append(summ)

            with open(doneList_fp, 'a', encoding="utf-8") as f:
                f.write(fp + '\n')
        
    save_tmp(summsTopic, summsPN, summsTD, summsTotal, meeting_name)
    
    summ_topicwise = text_summs2summ_topicwise(summsTopic, gptModel).split(': \"')[-1].split('\"')[0]
    summ_posneg = text_summs2summ_posneg(summsPN, gptModel).split(': \"')[-1].split('\"')[0]
    summ_todo = text_summs2summ_todo(summsTD, gptModel).split(': \"')[-1].split('\"')[0]
    summ_total = text_summs2summ_total(summsTotal, gptModel).split(': \"')[-1].split('\"')[0]
    
    return summ_topicwise, summ_posneg, summ_todo, summ_total

# def summarize_audio(audio_file, do_compress=False):
#     full_transcript = stt(audio_file, do_compress)
    
#     sentences = sep_sentences(full_transcript)
    
#     # save_log(sentences, f"data/{re.sub("wav", "txt", audio_file)}")
#     save_log(sentences, f"{re.sub("wav", "txt", audio_file)}")
             
#     gptModel = "gpt-4o"
#     summ_topicwise = text_summ_topicwise(" ".join(sentences), gptModel)
#     summ_posneg = text_summ_posneg(" ".join(sentences), gptModel)
    
#     print("====================================")
#     print("topicwise summary ==================")
#     print("====================================")
#     print(summ_topicwise)
    
#     print("====================================")
#     print("positive negative summary ==========")
#     print("====================================")
#     print(summ_posneg)
    
#     return summ_topicwise, summ_posneg

# =============================================================================================
# =============================================================================================
# =============================================================================================
# =============================================================================================
# =============================================================================================
# =============================================================================================

def predict(text, model, tokenizer, device):
    # 입력 문장 토크나이징
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True)
    inputs = {key: value.to(device) for key, value in inputs.items()}  # 각 텐서를 GPU로 이동

    # 모델 예측
    with torch.no_grad():
        outputs = model(**inputs)

    # 로짓을 소프트맥스로 변환하여 확률 계산
    logits = outputs.logits
    probabilities = logits.softmax(dim=1)

    # 가장 높은 확률을 가진 클래스 선택
    pred = torch.argmax(probabilities, dim=-1).item()

    return pred, probabilities


def apply_model(texts, mode):
    wts_path = f'./wts/toxic/fine_tuned_koelectrasmall_{mode}_final'

    # 모델, 토크나이저 정의
    model = AutoModelForSequenceClassification.from_pretrained(wts_path)
    tokenizer = AutoTokenizer.from_pretrained(wts_path)

    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    model = model.to(device)
    model.eval()

    preds = []
    probs = []
    for text in texts:
        pred, prob = predict(text, model, tokenizer, device)
        preds.append(pred)
        if mode == 'abuse':
            probs.append(prob[0][1])
        elif mode == 'immoral':
            probs.append(prob[0][0])
    return preds, probs

def detect_toxic(texts):
    # preds_ab: 0이면 욕설X, 1이면 욕설
    preds_ab, probs_ab = apply_model(texts, mode='abuse')
    # preds_im: 0이면 비윤리, 1이면 비윤리x
    preds_im, probs_im = apply_model(texts, mode='immoral')

    # preds_im: 0이면 비윤리x, 1이면 비윤리, 컷을 확률 70%로
    preds_im = [0 if prob_im < 0.7 else 1 for prob_im in probs_im]

    # preds_toxic: 0이면 독성x, 그 외엔 독성
    preds_toxic = [pred_ab + pred_im for pred_ab, pred_im in zip(preds_ab, preds_im)]
    
    idx_toxic = list(filter(lambda x: preds_toxic[x] != 0, range(len(preds_toxic))))

    return idx_toxic #, probs_ab, probs_im

# =============================================================================================
# =============================================================================================
# =============================================================================================
# =============================================================================================
# =============================================================================================
# =============================================================================================

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