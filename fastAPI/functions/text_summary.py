import os
import openai
from openai import OpenAI
import base64

def load_key():
    with open('./my_openai_api.txt', 'r') as f:
        openAI_key = f.readline().strip()
    return openAI_key

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

openai.api_key = load_key()
os.environ['OPENAI_API_KEY'] = openai.api_key

def summary(gptModel):
    input_text = '오늘 저희 음성 데이터를 어떻게 백엔드 서버로 보낼지, 그리고 백엔드에선 이걸 어떻게 처리할지 결정해야돼요. Pyannote 찾아보니까 cpu 서버에서 돌리면 실시간으로 어려울 것 같던데, 저희 음성 요약이나 독성탐지를 굳이 실시간으로 해야할까요? \
        저는 실시간일 필요가 없다고 생각합니다. 저두요. \
        그러면 음성은 후처리로 하는 것으로 하고. 영상은 30초마다 백엔드로 보내서 처리하는건 이미 구현해놨는데, 이게 30초가 너무 긴 것 같아요. 10초, 20초, 어떤게 좋아보이세요?\
        짧을 수록 좋긴 한데, 병목 현상이 있지는 않을까요? 일단 해보죠? 그럼 일단 해보고 결정하는거로 합시다.'

    # print('원본 회의록\n', input_text, '\n\n')
    # print('주제별 요약\n', text_summ_topicwise(input_text, gptModel), '\n\n')
    # print('긍정-중립-부정-요약\n', text_summ_posneg(input_text, gptModel), '\n\n')
    # print(text_summ_total(input_text, gptModel))

    image_path = './capture03.png'
    print('그림 요약\n', image_summ(image_path, gptModel))