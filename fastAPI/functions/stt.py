import os
import argparse
import time
import json
import subprocess
import torch
from faster_whisper import WhisperModel
from faster_whisper.transcribe import BatchedInferencePipeline

os.environ['KMP_DUPLICATE_LIB_OK'] = 'True'

if torch.cuda.is_available():
    DEVICE = "cuda"
else:
    DEVICE = "cpu"
    print("diarization을 cpu로 할 경우 음성시간이 2배 정도가 소요될 것으로 에상됩니다.\n gpu 환경에서 사용하시길 권장합니다.")

# # Define API key file path
# API_KEY_PATH = "./my_openai_api.txt"

# def load_key():
#     with open(API_KEY_PATH, 'r') as f:
#         openAI_key = f.readline().strip()
#     return openAI_key

# openai.api_key = load_key()
# os.environ['OPENAI_API_KEY'] = openai.api_key


# API 기반 Whisper
# def audio_to_text(audio_file):
#     client = OpenAI()
#     # STT using Whisper (OpenAI)
#     transcript = client.audio.transcriptions.create(
#         file=audio_file,
#         model="whisper-1",
#         language="ko",
#         response_format="text"
#     )
#     return transcript

def get_text_from_sound(audio_path):
    model_size = "large-v3"
    model = WhisperModel(model_size, device=DEVICE, compute_type="int8")
    batched_model = BatchedInferencePipeline(model=model)

    print("STT working!")
    results, _ = batched_model.transcribe(audio_path, language="ko", vad_filter=True, vad_parameters=dict(min_silence_duration_ms=2000), batch_size=8)
    transcript = [result.text for result in list(results)]
    print("DONE!")

    return transcript

# Pretrained model Whisper 사용
def apply_fasterWhisper(audio_path, output_file):
    t5 = time.time()
    transcript = get_text_from_sound(audio_path)
    t6 = time.time()
    print('STT Only: ', t6-t5)

    print('Writing JSON File!')
    t7 = time.time()
    with open(output_file, "w", encoding="utf-8") as f:
        json.dump(transcript, f, ensure_ascii=False, indent=4)
    t8 = time.time()
    print('Done!')
    print('JSON Making: ', t8-t7)

    # Remove compressed file
    compressed_file = f"{os.path.splitext(audio_path)[0]}_compressed.mp3"
    if os.path.exists(compressed_file):
        os.remove(compressed_file)
        
    return transcript

# def load_audio(audio_path):
    # audio_file = open(audio_path, 'rb')
    # return audio_file

# =============================================================================================
# =============================================================================================
# =============================================================================================
# Audio compression
## Get ffmpeg path
# if os.name == 'nt':  # Windows system
#     try:
#         ffmpeg_path = subprocess.check_output(['where', 'ffmpeg']).decode().strip()
#     except:
#         print('Please make sure ffmpeg is installed and added to the PATH environment variable')
#         exit()
# else:  # Linux and Mac systems
#     try:
#         ffmpeg_path = subprocess.check_output(['which', 'ffmpeg']).decode().strip()
#     except:
#         print('Please make sure ffmpeg is installed and added to the PATH environment variable')
#         exit()

def compress_audio(input_file):
    # Get input file size
    file_size = os.path.getsize(input_file)
    if file_size <= 25000000:
        return input_file

    # Compress audio file using ffmpeg
    output_file = f"{os.path.splitext(input_file)[0]}_compressed.mp3"
    command = f"{ffmpeg_path} -i {input_file} -ac 1 -ar 16000 -ab 16k {output_file} -loglevel quiet"
    os.system(command)

    return output_file

# =============================================================================================
# =============================================================================================
# =============================================================================================

def call_args():
    parser = argparse.ArgumentParser()
    parser.add_argument('--do_compress', type=int, default=1, choices=[0, 1])
    args = parser.parse_args()

    if args.do_compress == 0: args.do_compress = True
    elif args.do_compress == 1: args.do_compress = False
    return args

def run(input_file, do_compress=True):
    output_format = 'json'
    file_name, _ = os.path.splitext(input_file)
    output_file = f"{file_name}_transcript.{output_format}"

    if do_compress:
        # Compress audio file if larger than 25MB
        print('Compress Audio file...')
        t1 = time.time()
        input_file = compress_audio(input_file)
        t2 = time.time()
        print('Done!')
        print('Compression time: ', t2-t1)
    
    t3 = time.time()
    transcript = apply_fasterWhisper(input_file, output_file)
    t4 = time.time()
    print('Whisper time: ', t4-t3)
    
    return transcript

if __name__ == '__main__':
    # compress_audio('data/audio1.m4a')
    args = call_args()
    run('data/audio1-3.m4a', args.do_compress)