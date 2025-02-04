from fastapi import FastAPI, File, UploadFile, BackgroundTasks, Form, Depends
from datetime import datetime
from sqlalchemy.orm import Session
# from functions.audio_recording import *
from functions.speaker_diarization import *
from functions.backend_ai_audio import *
from functions.backend_ai_video import load_crop_img_by_bytes, check_fatigue
import re
import cv2
import base64
import librosa
import soundfile as sf
from fastapi.middleware.cors import CORSMiddleware  # 추가된 import
from fastapi.responses import JSONResponse, Response
from database import get_db
app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:8181",
                   "http://localhost:8080",
                   "http://127.0.0.1:8080",
                   "http://localhost:9292"],  # 보안을 위해 실제 운영 환경에서는 특정 도메인을 지정하세요
    allow_credentials=True,
    allow_methods=["*"],  # 허용할 HTTP 메서드 지정
    allow_headers=["*"],  # 허용할 HTTP 헤더 지정
)

@app.get("/")
def read_root():
    return {"Hello": "World"}

@app.post("/postRecord")
async def get_record(record: UploadFile = File(...)):
    if record.filename.endswith("wav"):
        print("wav 파일이 들어왔습니다. ")
        file_location = f"temp_{record.filename}"

        with open(file_location, "wb") as buffer:
            buffer.write(await record.read())
    else:
        raise ValueError("잘못된 형식의 녹음 파일이 전송되었습니다. wav로 변환해서 보내주세요.")
        
    recording_start_time = datetime.now()
    
    results = process_audio(file_location, recording_start_time)
    
    return results
    # except Exception as e:
    #     return {"error": str(e)}
    return {"Hello": "World"}

@app.post("/postScreen")
def get_screen(screen: UploadFile = File(...)):
    return {{"Hello": "World"}}

@app.post("/post_audio")
async def post_audio(audio: UploadFile = File(...), background_task:BackgroundTasks = BackgroundTasks()): 
    file_location = f"temp_{audio.filename}"

    with open(file_location, "wb") as buffer:
        buffer.write(await audio.read())

    # background_task = BackgroundTasks()
    background_task.add_task(audio_to_text_by_pyannote, file_location)

    return "hello"

# 기존 라우트 핸들러들
@app.post("/post_audio_test")
async def post_audio(audio: UploadFile=File(...),):
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

@app.post("/summarize_screen")
async def post_screen(image: UploadFile=File(...), meeting_name:str = Form(...)):
    if not os.path.exists(meeting_name):
        os.mkdir(meeting_name)
    
    file_location = f"{meeting_name}/temp_{image.filename}"
    with open(file_location, "wb") as buffer:
        buffer.write(await image.read())
    summ_img = image_summ(file_location, "gpt-4o").split(': \"')[-1].split('\"')[0]
    
    return summ_img
    

@app.post("/detect_fatigue")
async def detect_fatigue(image: UploadFile=File(...)):
    bytes = await image.read()
    
    imgs, mode = load_crop_img_by_bytes(bytes)
    
    fratio, _, results_imgs = check_fatigue(imgs, mode)
    
    _, encoded_img = cv2.imencode(".jpg", results_imgs)
    
    base64_img = base64.b64encode(encoded_img).decode("utf-8")
    
    return JSONResponse(content={"image": base64_img, "fratio": fratio.item()})
    # return JSONResponse(content={"image": encoded_img.tobytes(), "fratio": fratio.item()})
    # return {"image": encoded_img.tobytes(), "fratio": fratio.item()}
    # return Response(content=encoded_img.tobytes(), media_type="image/jpeg", headers={"fr": "0.6"})
    

@app.post("/summarize_meeting")
async def summarize_meeting(audio:UploadFile=File(...), meeting_name:str = Form(...), status:str=Form("ing"), 
                            background_task:BackgroundTasks = BackgroundTasks(), db: Session = Depends(get_db)):
    if not os.path.exists(meeting_name):
        os.mkdir(meeting_name)
        
    audio_name = audio.filename
    
    file_location = f"{meeting_name}/audio_{len(os.listdir(meeting_name))}.{audio_name.split(".")[-1]}"
    
    with open(file_location, "wb") as buffer:
        buffer.write(await audio.read())

    if status == "ing":
        summ_topicwise, summ_posneg, summ_todo, summ_total = summarize_audio(meeting_name)
        return {"topicwise": summ_topicwise, "posneg": summ_posneg, "todo": summ_todo, "total": summ_total}
    elif status == "end":
        background_task.add_task(summarize_audio, meeting_name)
        background_task.add_task(audio_to_text_by_pyannote, file_location, meeting_name)
        return "회의 고생하셨습니다."
        
        
        
        

# @app.post("/summarize_meeting")
# async def summarize_meeting(audio:UploadFile=File(...), meeting_name:str = Form(...), status:str=Form("ing"), background_task:BackgroundTasks = BackgroundTasks()):
#     if not os.path.exists(meeting_name):
#         os.mkdir(meeting_name)
        
#     file_name = audio.filename
    
#     file_location = f"{meeting_name}/{file_name}"
    
#     with open(file_location, "wb") as buffer:
#         buffer.write(await audio.read())
    
#     if status=="end":
#         # 음성 데이터 합치고 
#         audio_files = os.listdir(meeting_name)
#         audio_files = [audio_file for audio_file in audio_files if audio_file.endswith("wav")]
        
#         y_ref, sr_ref = sf.read(f"{meeting_name}/{audio_files[0]}")
        
#         merged_audio = np.zeros((y_ref.shape[-1], 1))
        
#         for audio_file in audio_files:
#             y, sr = sf.read(f"{meeting_name}/{audio_file}")
            
#             if sr != sr_ref:
#                 y = librosa.resample(y.T, orig_sr=sr, target_sr=sr_ref).T
                
#             if len(y.shape) == 1:
#                 y = np.expand_dims(y, axis=1)
                
#             merged_audio = np.concatenate((merged_audio, y), axis=0)
            
#         file_location = f"{meeting_name}/concat.wav"
        
#         sf.write(file_location, merged_audio, sr_ref)
        
#         # 합친 파일을 던져주고
#         background_task.add_task(summarize_audio, file_location)
#         background_task.add_task(audio_to_text_by_pyannote, file_location)
        
#     else:
#         summ_topicwise, summ_posneg = summarize_audio(file_location)
        
#         result = {"topicwise": summ_topicwise, "posneg": summ_posneg}
        
#         return result      

###########################################################################
####################### Models ############################################
###########################################################################


###########################################################################
####################### functions #########################################
###########################################################################
def audio_to_text_by_pyannote(file_location, meeting_name):
    recording_start_time = datetime.now()

    voice_dir = "voice"

    output_dir = re.sub(":", "_", str(recording_start_time)).split(".")[0]

    pyannote_result = predict_by_pyannote(file_location, output_dir, voice_dir, meeting_name)
        
    for name, pyannote_value in zip(pyannote_result.keys(), pyannote_result.values()):
        pyannote_predict = pyannote_value["predict"]
        script = pyannote_value["script"]
        toxicity = pyannote_value["toxicity"]
        print(f"{name}: script {script} // speaker {pyannote_predict} // toxicity {toxicity}")

def background_summarize(meeting_name):
    summ_topicwise, summ_posneg, summ_todo, summ_total = summarize_audio(meeting_name)
    

###########################################################################
####################### AI Code ###########################################
###########################################################################

