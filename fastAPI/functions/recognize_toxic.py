import torch
import argparse
import matplotlib.pyplot as plt
import pandas as pd
from transformers import ElectraModel, ElectraTokenizer, AutoTokenizer, AutoModelForSequenceClassification, Trainer, TrainingArguments, EarlyStoppingCallback, pipeline, AdamW
from datasets import load_dataset, Dataset

from sklearn.model_selection import train_test_split
from sklearn.metrics import *
from sklearn.preprocessing import LabelEncoder

from tqdm import tqdm
import os
from warnings import filterwarnings
filterwarnings('ignore')

_device = torch.device("cuda" if torch.cuda.is_available else "cpu")

def predict(text, model, tokenizer, target):
    # 입력 문장 토크나이징
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True)
    inputs = {key: value.to(_device) for key, value in inputs.items()}  # 각 텐서를 GPU로 이동

    # 모델 예측
    with torch.no_grad():
        outputs = model(**inputs)

    # 로짓을 소프트맥스로 변환하여 확률 계산
    logits = outputs.logits
    probabilities = logits.softmax(dim=1)

    # 가장 높은 확률을 가진 클래스 선택
    pred = torch.argmax(probabilities, dim=-1).item()

    pred = classify_target(pred, target)

    return pred, probabilities

def classify_target(idx, target):
    if target == "abuse":
        result = {0: "not_abusive", 1:"abusive"}
    else:
        return idx
    
    return result[idx]

def predict_txt(txt, model, tokenizer, target):
    pred_json = {
        "status": 200,  # 원래는 받은 결과로 확인. status.OK등으로 
        "result":[]
    }
    
    model.to(_device)
    
    preds = []
    probs = []
    if type(txt) == str:
        pred, prob = predict(txt, model, tokenizer, target)
        pred_json["result"].append({
            "sentence": txt,
            "toxic": pred,
            "toxic_probability": prob
        })
    elif type(txt) == list and txt and type(txt[0]) == str:
        for temp_txt in txt:
            pred, prob = predict(temp_txt, model, tokenizer, target)
            pred_json["result"].append({
                "sentence": temp_txt,
                "toxic": pred,
            })
            
    return pred_json

def recognize_abuse(text):
    save_fp = "/abuseModel"
    target = "abuse"
    model = AutoModelForSequenceClassification.from_pretrained(save_fp)
    tokenizer = AutoTokenizer.from_pretrained(save_fp)
    
    predict(text, model, tokenizer, target)