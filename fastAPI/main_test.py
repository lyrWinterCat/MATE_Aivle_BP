from fastapi import FastAPI, File, UploadFile, Form, Query

app = FastAPI()

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