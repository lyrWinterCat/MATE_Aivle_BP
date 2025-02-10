## 얼굴 피곤 탐지 커스텀 데이터 생성

원버튼 딸깍 실행파일 만들고 싶었으나 잘 안돼서 그냥 가이드라인 제작합니다.

CMD에서 실행하시는 것을 추천드립니다.

1. 가상환경 생성

| conda create -y -n face python=3.12.3

2. 가상환경 실행

| conda activate face

3. Face Recognition 라이브러리 설치를 위한 dlib 설치

| conda install -c conda-forge dlib

4. 라이브러리 설치

| pip install -r requirements.txt

5. 파일 실행

| python mk_faceDataset.py --name [저장될 파일에 들어갈 여러분 이름; e.g., yun] --num [저장할 데이터 수; default=1000]


<br/>

<br/>

## 결과물 확인 후 원하는 파일 세트로 삭제하는 방법

1. ../data/custom/bboxed/ 경로에서 지울 이미지를 골라 삭제한다.

2. python mk_faceDataset.py --mode del 실행



<br/>

<br/>

## "난 CMD 가 싫어요. VSCode가 좋아요!" 하시는 분들

1. VSCode를 킨다.

2. 경로 설정을 mk_faceDataset.py가 있는 폴더로 해준다.

3. call_args() 함수 내의 --name과 --num 의 default= 옆의 값을 원하는 값으로 수정해서 실행

4. 파일 삭제를 원하시면 ../data/custom/bboxed/ 경로에서 지울 이미지를 골라 삭제 후 --mode의 default= 옆의 값을 del로 수정해서 실행

![image](./call_args.PNG)
