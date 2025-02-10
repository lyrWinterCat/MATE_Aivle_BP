## FASTAPI 서버 코드

GPU 환경을 권장합니다. 또한 GPU 환경에서 사용할 경우 pytorch는 본인의 환경에 맞춰서 설치하셔야 합니다.

1. 가상환경 생성 및 실행

| conda create -y -n fastapi python=3.12.3

| conda activate fastapi

2. Library 설치

| pip install -r requirements.txt

3. fastAPI 실행

| uvicorn main:app 

<br/>

## 필수 파일 
1. DB_URL.txt: mysql+aiomysql://db.address/url (mySql 기준으로 작성)<br>
(Optional) DigiCertGlobalRootCA.crt.pem: SSL 인증키가 필요하면 있어야하며, 필요 없을 경우 코드 내부 수정 필요.
2. huggingFace_token.txt: speakerdiarization에 사용되는 모델은 hugging face의 token이 필요함. token 발급 관련은 아래에 존재.
3. my_openai_api.txt: openai api key 값이 입력된 텍스트 파일

<br/>

## huggingFace token 발급 방법
huggingface의 토큰은 일단 발급을 받아야하는데 아래와 같은 방식으로 먼저 발급을 받으면 됩니다. 

https://kjh1337.tistory.com/3

그 후, https://huggingface.co/pyannote/speaker-diarization-3.0와 같은 페이지에서 모델 사용 권한을 각각 받아야합니다. 
speaker-diarization-3.0, segmentation, embedding 등의 모델에 권한을 받아야합니다. 
