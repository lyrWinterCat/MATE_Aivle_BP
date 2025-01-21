from fastapi import FastAPI, File, UploadFile, Form, Query
from fastapi.middleware.cors import CORSMiddleware  # 추가된 import

app = FastAPI()

# CORS 미들웨어 설정 추가
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # 보안을 위해 실제 운영 환경에서는 특정 도메인을 지정하세요
    allow_credentials=True,
    allow_methods=["*"],  # 허용할 HTTP 메서드 지정
    allow_headers=["*"],  # 허용할 HTTP 헤더 지정
)

# 기존 라우트 핸들러들
@app.post("/post_audio")
async def post_audio(audio: UploadFile=File(...)):
    file_location = f"temp_{audio.filename}"
    with open(file_location, "wb") as buffer:
        buffer.write(await audio.read())
    return "오디오 전송 완료"

@app.post("/post_image")
async def post_image(image: UploadFile=File(...)):
    file_location = f"temp_{image.filename}"
    with open(file_location, "wb") as buffer:
        buffer.write(await image.read())
    return "이미지 전송 완료"