import librosa
import soundfile as sf
import numpy as np
import os 
import torch 
import re
import json 
import joblib 

# from resemblyzer import VoiceEncoder, preprocess_wav, sampling_rate
from scipy.spatial.distance import cosine, cdist
# from pydub import AudioSegment
from datetime import timedelta
from pyannote.audio import Pipeline, Model, Audio, Inference
from pyannote.core import Segment
from pyannote.audio.pipelines.speaker_verification import PretrainedSpeakerEmbedding
from pyannote.audio.pipelines import SpeakerDiarization

from functions.backend_ai_audio import detect_toxic
from functions.stt import *

with open("huggingFace_token.txt", "r") as f:
    HUGGINFACE_TOKEN = f.readline()

print(HUGGINFACE_TOKEN)

# RESEMBLYZER_ENCODER = VoiceEncoder(device=torch.device(DEVICE))
# 모델을 먼저 불러와야 추후 음성파일이 전달될 때 소요시간을 줄일 수 있음. 
pretrained_model = Pipeline.from_pretrained("pyannote/speaker-diarization-3.1", use_auth_token=HUGGINFACE_TOKEN)

model_pyannote = Model.from_pretrained("pyannote/wespeaker-voxceleb-resnet34-LM") # 이 부분은 fine_tuned 모델로 변경 예정.

model_pyannote = model_pyannote.to(torch.device(DEVICE))

fine_tuned_model = Model.from_pretrained("diarization_model/max_1_index_1.ckpt")

fine_tuned_pipeline = SpeakerDiarization(
    segmentation=fine_tuned_model,
    embedding=pretrained_model.embedding,
    embedding_exclude_overlap=pretrained_model.embedding_exclude_overlap,
    clustering=pretrained_model.klustering,
)

fine_tuned_pipeline.instantiate(
    {
        "clustering": {
            "method": "centroid",
            "min_cluster_size": 15,
            "threshold": 0.7895802598828106,
        },
        "segmentation": {
            "min_duration_off": 0.5,
            "threshold": 0.47465348027591897,
        },
    }
)
fine_tuned_pipeline = fine_tuned_pipeline.to(torch.device(DEVICE))


# fine_tuned_inference = Inference(pipeline, window="whole")
inference = Inference(model_pyannote, window="sliding") # embedding 모델이라고 생각하면 됨. 음성파일을 하나의 벡터로 만들기 위한 함수.  
# RESEMBLYZER_MODEL = joblib.load("diarization_model/resemblyzer.pkl")

diarization_pipeline = pretrained_model.to(torch.device(DEVICE))

#############################################################
############## pyannote #####################################
#############################################################
def get_pyannote_embbeding(INFERENCE, voice_dir):
    # 나중에 비교할 음성들을 가져오기 위한 것
    voice_files = os.listdir(voice_dir)

    reference_embeddings = {
        voice.split(".")[0]: INFERENCE(f"{voice_dir}/{voice}")
        for voice in voice_files
    }

    return reference_embeddings

def save_audio_segment(input_file, start_time, end_time, output_file):
    y, sr = librosa.load(input_file, sr=None)
    
    start_sample = int(start_time * sr)
    end_sample = int(end_time * sr)
    
    sf.write(output_file, y[start_sample:end_sample], sr)

def merge_audio_files(audio_files, meeting_name):
    y_ref, sr_ref = sf.read(f"{meeting_name}/{audio_files[0]}")
    
    merged_audio = np.zeros((y_ref.shape[-1], 1))
    
    for audio_file in audio_files:
        y, sr = sf.read(f"{meeting_name}/{audio_file}")
        
        if sr != sr_ref:
            y = librosa.resample(y.T, orig_sr=sr, target_sr=sr_ref).T
            
        if len(y.shape) == 1:
            y = np.expand_dims(y, axis=1)
            
        merged_audio = np.concatenate((merged_audio, y), axis=0)
        
    file_location = f"{meeting_name}/total.wav"
    
    sf.write(file_location, merged_audio, sr_ref)
    
    return file_location
    

def predict_by_pyannote(output_dir, voice_dir, meeting_name, diarization_pipeline=pretrained_model):
    print("하나의 파일로 만들겠습니다")
    audio_files = [audio_file for audio_file in os.listdir(meeting_name) if audio_file.endswith("wav")]
    if "total.wav" in audio_files:
        file_location = f"{meeting_name}/total.wav"
        print('통합본이 있습니다. 해당 통합본으로 진행합니다.')
    else:
        file_location = merge_audio_files(audio_files, meeting_name)
        print(f"통합완료. 파일명: {file_location.split('/')[-1]}")
    print('화자 구분 프로세스를 시작합니다.')
    diarization_result = diarization_pipeline(file_location)
    print('화자 구분 프로세스가 끝났습니다.')

    reference_embeddings = get_pyannote_embbeding(inference, voice_dir)

    print("화자 별 구간 및 저장 프로세스 진행합니다.")
    idx = 0

    output_dir = output_dir

    if not os.path.exists(output_dir):
        os.mkdir(output_dir)

    result_dict = {}
    
    for segment, _, speaker in diarization_result.itertracks(yield_label=True):
        try:
            segment_embedding = inference.crop(file_location, segment, duration=inference.duration)
            
            try:
                similarity = {
                    name: float(cosine(segment_embedding[0,:,0], ref_embedding[0,:,0]))
                    for name, ref_embedding in reference_embeddings.items() if ref_embedding is not None
                }
            except:
                similarity = {
                    name: float(cosine(segment_embedding[0,:], ref_embedding[0,:]))
                    for name, ref_embedding in reference_embeddings.items() if ref_embedding is not None
                }
            

            similarity = dict(sorted(similarity.items(), key=lambda x: x[1], reverse=True))

            predict = max(similarity, key=similarity.get).split("_")[0]

            # 음성 구분 구간 정리
            segment_start = segment.start
            segment_end = segment.end

            speaker_audio_file = f"{idx}_{int(segment_start):04d}_{int(segment_end):04d}.wav"
            output_file = f"{output_dir}/{speaker_audio_file}"

            save_audio_segment(file_location, segment_start, segment_end, output_file)

            transcript = get_text_from_sound(output_file)
            
            toxicity = detect_toxic([transcript])
            
            if toxicity:
                toxicity = 1
            else:
                toxicity = 0
            result_dict[speaker_audio_file] = {"predict": predict, "script": transcript, "toxicity": toxicity, "start": segment_start}

            idx = idx + 1
        except Exception as e:
            print(e)

    with open("test.json", "w", encoding="utf-8") as f:
        json.dump(result_dict, f)

    return result_dict

#############################################################
############## resemblzyer ##################################
#############################################################
# def get_resemblyzer_embedding(voice_dir): 
#     # 이부분 수정 필요, 정확한 데이터 형태 정할 필요가 있음. 
#     # 이게 rttm으로 정리된 파일을 얻는거라서 아마 잘 되지 않을까 싶음. 
#     wav_files = os.listdir(voice_dir)
#     embeddings = []

#     for wav_file in wav_files:
#         wav = preprocess_wav(f"{voice_dir}/{wav_file}")
#         embedding = RESEMBLYZER_ENCODER.embed_utterance(wav)
#         embeddings.append(embedding)

#     return embeddings

# def predict_by_resemblyzer(voice_dir):
#     wav_files = os.listdir(voice_dir)
#     embeddings = get_resemblyzer_embedding(voice_dir)
#     predictions = RESEMBLYZER_MODEL.predict(embeddings)

#     result_dict = {}
#     for wav_file, predict in zip(wav_files, predictions):
#         result_dict[wav_file] = predict

#     return result_dict