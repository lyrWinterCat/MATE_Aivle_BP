<head>
  <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
</head>

# AI 스마트 회의 어시스턴트 MATE  

## 📹Demo 시연

![demo_gif](./sample_imgs/demo_width600.gif)

음성이 포함된 동영상은 다운받아주세요. [Full video](./sample_imgs/demo.mp4)


## 🗓 개발 기간

2025.01.02 ~ 2025.02.13




## 🤝 팀 구성

| 이름   | 주 업무 | Link |
|--------|---------|------|
| 이예림 | PL      |  <a href="https://github.com/lyrWinterCat"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-181717.svg?&style=for-the-badge&logo=GitHub&logoColor=white"/>    |
| 안지홍 | FE      |  <a href="https://github.com/dnwn129"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-181717.svg?&style=for-the-badge&logo=GitHub&logoColor=white"/>    |
| 이승은 | SE      |  <a href="https://github.com/Greeense"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-181717.svg?&style=for-the-badge&logo=GitHub&logoColor=white"/>    |
| 고수현 | DE      |  <a href="https://github.com/csj8566"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-181717.svg?&style=for-the-badge&logo=GitHub&logoColor=white"/>    |
| 윤찬혁 | AI      |  <a href="https://github.com/ChanhyukYun"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-181717.svg?&style=for-the-badge&logo=GitHub&logoColor=white"/> |
| 박언용 | BE      |  <a href="https://github.com/onionpark"><img alt="GitHub" src="https://img.shields.io/badge/GitHub-181717.svg?&style=for-the-badge&logo=GitHub&logoColor=white"/>    |




## 🗃 선정 배경

- 코로나가 창궐한 2020년 이후 온라인 화상 회의 시장 규모는 점진적으로 증가함

- 기존 온라인 회의 서비스는 실시간 요약 기능이나 회의록 저장 기능 등이 기본적으로 지원되지 않음

- 강남역 근처에서 93명에 대하여 설문조사 결과, 온라인 회의는 다음 문제가 있음

  > 1. 오프라인 회의 대비 집중하기 어려움
  > 2. 참가자 간 소통이 어려움
  > 3. 회의 내용 정리가 어려움

- 또한 온라인 회의 내용 정리는 대부분 직접 회의록을 필기하거나 문서 작성해서 이뤄지며 이에 불만이 많아, 회의 내용 자동 요약이 필요하다는 응답이 많았음

- 요약 기능 외에도 회의록 작성이나 공유 자료 요약 기능, 그리고 독성 발언 탐지 기능 또한 필요함




## 🛠 주요 서비스

1. 회의 내용 및 공유 자료 실시간 요약

2. 회의 내용 기반 ToDo List 제공

3. 연속 회의를 위한 이전 회의 정보 제공

4. 회의 중 참여자 피로탐지

5. 회의 중 독성 발언 탐지


## ⚡적용된 AI 기술

|   |          **AI 기술**         | **세부 내용**                                                        |
|---|:----------------------------:|----------------------------------------------------------------------|
| 1 | Faster-Whisper<br/>(Pre-trained) | - Speech-to-Text (STT) <br/>- API 기반 Whisper 대비 약 3배 빠른 추론 속도 |
| 2 |      GPT-4o<br/>(OpenAI API)     | - 텍스트 요약 <br/>- 이미지 요약                                          |
| 3 |    KoELECTRA<br/> (Fine-tuned)    | - 회의 음성 텍스트 중 독성 표현 (욕설, 비윤리적 발언) 탐지           |
| 4 |     Pyannote<br/> (Fine-tuned)    | - 회의 중 발화자 식별                                                |
| 5 |      YOLO11<br/> (Fine-tuned)     | - 회의 참여자 얼굴 기반 피로 탐지                                    |


## 📈 기대효과

#### 다양한 회의 피드백

- 좋은 성과를 낸 아이디어 제공자 식별 및 포상 가능

- 회의 중 독성 발언 감시로 건전한 회의 분위기 조성 가능

#### 업무 효율성 향상

- 회의 중 또는 이후 요약 기능 제공으로 원활한 회의 진행 및 복기 가능

- 주제별 요약 및 ToDo List 제공으로 불필요한 논의 최소화

#### 회의 스트레스 감소

- 집중도 저하 시 피로 탐지 기반의 적절한 휴식 권고로 밀도 있는 회의 진행 가능

- 회의 중 독성 발언으로 인한 감정적 스트레스 감소가 기대됨


## ⚙ 사용 방법

#### 1. FastAPI 환경설정

#### 2. API 키 설정

#### 3. Spring 및 FastAPI 서버 실행

#### 4. 서비스





## 🤔새로 배울 수 있었던 점
