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

## pyannote 데이터 준비
준비물: 음성파일, 음성파일의 정보가 담긴 uem 파일, 화자의 정보가 담긴 rttm파일, 데이터 경로가 담긴 yaml파일

1. 음성파일: mp4, wav 등의 확장자로 되어있는 음성파일
2. [uem 파일](./AI/speakerdiarization/pyannote/train/uem/train1.uem): {파일명} <NA> {시작시간} {끝나는시간} 의 형태로 데이터가 작성되어있는 파일
3. [rttm 파일](./AI/speakerdiarization/pyannote/train/rttm/train1.rttm): SPEAKER {파일명} 1 {시작시간} {음성길이} <NA> <NA> {발화자명} <NA> <NA> 의 형태로 데이터가 작성되어있는 파일
4. [yaml 파일](./AI/speakerdiarization/pyannote/database.yml): 데이터(음성파일, uem, rttm)의 경로가 작성되어있는 파일

## pyannote 학습 파일
[compare_der.py](./AI/speakerdiarization/compare_der.py)

해당 학습 파일은 10번 정도 새롭게 학습을 하여 총 10개의 학습모델을 생성하고 해당 학습 모델의 성능을 수치로 볼 수 있도록 log 파일 생성. <br>
로그를 확인하여 가장 좋은 모델을 선택하여 추후 fine_tuning 진행

% 주의 : 해당 학습은 ubuntu 환경에서 사용(window 환경에서는 불가능 - 원할시 WSL 사용)

## fastapi endpoint
fastapi endpoint들은 Form-data로 variable값을 POST로 받게되어있음. 

1. summarize_meeting: 휴식하기 or 회의종료를 눌렀을 때 4가지의 요약 및 발화자 구분을 해주는 함수<br>
필요 variable: audio, meeting_name, status<br>
audio: File / 음성파일<br>
meeting_name: string / 회의명<br>
status: string / "ing" or "end" => 휴식하기를 눌렀을 때는 "ing", 회의종료를 눌렀을 때는 "end"로 받게 되어있음.<br>
![summarize_meeting](./sample_imgs/summarize_meeting.png)
2. detect_fatigue: 피로도 측정해서 측정 결과를 보내주는 함수<br>
필요 variable: image <br>
image: File / 온라인 회의 화면 이미지<br>
![detect_fatigue](./sample_imgs/detect_fatigue.png)
3. summarize_screen: 공유된 화면의 내용을 정리해주는 함수<br>
필요 variable: image, meeting_name<br>
image: File / 공유된 화면 이미지<br>
meeting_name: string / 회의명<br>
![summarize_screen](./sample_imgs/summarize_screen.png)
