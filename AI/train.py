import torch
import argparse
import pandas as pd
from transformers import AutoTokenizer, AutoModelForSequenceClassification, Trainer, TrainingArguments, EarlyStoppingCallback, DataCollatorWithPadding
from datasets import Dataset
from sklearn.model_selection import train_test_split
from sklearn.metrics import *
from sklearn.utils import resample
from torch.utils.data import DataLoader
import os
from warnings import filterwarnings
import numpy as np
from tqdm import tqdm
filterwarnings('ignore')

# 모델 학습을 위한 command-line arugment parsing
def call_args():
    parser = argparse.ArgumentParser("MATE model argument paramgeters")
    parser.add_argument('--modelType', type=str, choices=['kluebert', 'koelectra', 'kobertLM', 'koelectra2'], default='koelectra')
    parser.add_argument('--mver', type=str, default="small", help="version of basemodel, KoELECTRA; default=base")
    parser.add_argument('--epochs', type=int, default=10)
    parser.add_argument('--lr', type=float, default=2e-5)
    parser.add_argument('--batchsize', type=int, default=32)
    parser.add_argument('--num_labels', type=int, default=2)
    parser.add_argument('--mode', choices=['eval', 'train'], default='train')
    parser.add_argument('--save_fp', default='./wts/')
    parser.add_argument('--target', choices=['abuse', 'sentiment', 'offensiveness', 'immoral'], required=True)
    args = parser.parse_args()

    # abuse, offensiveness는 2로, sentiment는 3으로 고정
    if args.target == 'sentiment':
        args.num_labels = 3
    return args

# # Pretrained-model (KoELECTRA) 불러오기
def call_model(args, device):
    if args.modelType == 'koelectra':
        model_name = f"monologg/koelectra-{args.mver}-v3-discriminator"
    elif args.modelType == 'kobert':
        model_name = 'klue/bert-base'
    elif args.modelType == 'kobertLM':
        model_name = 'monologg/kobert-lm'
    elif args.modelType == 'koelectra2':
        model_name = 'Copycats/koelectra-base-v3-generalized-sentiment-analysis'

    if args.modelType == 'kobertLM':
        tokenizer = AutoTokenizer.from_pretrained(model_name, trust_remote_code=True)
    else:
        tokenizer = AutoTokenizer.from_pretrained(model_name)
    model = AutoModelForSequenceClassification.from_pretrained(model_name, num_labels = args.num_labels).to(device)
    model.train()
    return model, tokenizer

# 데이터 전처리, 데이터셋화
def load_data(tokenizer, args):
    if args.target in ['abuse', 'sentiment', 'offensiveness']:
        url = 'https://raw.githubusercontent.com/cardy20/KODOLI/refs/heads/main/data/kodoli.csv'
        data = pd.read_csv(url, sep=',')
        data.drop('index', axis=1, inplace=True)
    elif args.target in ['immoral']:
        url = './korean_immoral_texts.csv'
        data = pd.read_csv(url, sep=',')
        data.drop(data.columns[[0]], axis=1, inplace=True)

    if args.target == 'offensiveness':
        data.rename(columns={"sentences": "text", "offensiveness": "label"}, inplace=True)
        if args.num_labels == 3:
            data['label'] = data['label'].map({'NOT': 0, 'LIKELY': 1, 'OFFEN': 2})
        elif args.num_labels == 2:
            data['label'] = data['label'].map({'NOT': 0, 'LIKELY': 1, 'OFFEN': 1})

    elif args.target == 'sentiment':
        data.rename(columns={"sentences": "text", "sentiment": "label"}, inplace=True)
        data['label'] = data['label'].map({'POS': 0, 'NEU': 1, 'NEG': 2})

    elif args.target == 'abuse':
        data.rename(columns={"sentences": "text", "abuse": "label"}, inplace=True)
        data['label'] = data['label'].map({'NON': 0, 'ABS': 1})

    elif args.target == 'immoral':
        data.rename(columns={"text": "text", "is_immoral": "label"}, inplace=True)
        data['label'] = data['label'].map({True: 0, False: 1})

    train, val = train_test_split(data[['text', 'label']], test_size=0.2, random_state=42, stratify=data[['label']])
    tr_set = Dataset.from_pandas(train)
    val_set = Dataset.from_pandas(val)

    def preprocess_function(data):
        return tokenizer(data['text'], truncation=True, padding=True, 
                         pad_to_max_length=True, max_length=128
                         )
    
    train_ds = tr_set.map(preprocess_function, batched=True)
    val_ds = val_set.map(preprocess_function, batched=True)
    
    return train_ds, val_ds


# 학습 세팅 설정
def set_train(model, tokenizer, train_ds, val_ds, args):
    # 학습 하이퍼파라미터 지점
    training_args = TrainingArguments(
        output_dir = './results',          # 출력 디렉토리
        eval_strategy = "epoch",            # 에폭마다 평가
        save_strategy = "epoch",           # 에폭마다 체크포인트 저장

        learning_rate = args.lr,              # <--[조정가능]학습률
        per_device_train_batch_size = args.batchsize,  # <--[조정가능]학습 배치 사이즈
        per_device_eval_batch_size = args.batchsize,   # <--[조정가능]평가 배치 사이즈
        num_train_epochs  = args.epochs,              # <--[조정가능]에폭 수

        weight_decay = 0.02,               # <--[조정가능]weight decay
        load_best_model_at_end = True,     # 가장 좋은 모델을 마지막에 로드
        logging_dir ='./logs',            # 로깅 디렉토리
        logging_steps = 10,                # 로깅 스텝
        report_to="tensorboard"          # TensorBoard에 로깅
    )

    # Trainer 설정
    trainer = Trainer(
        model=model,                         # 학습할 모델
        args=training_args,                  # TrainingArguments
        train_dataset = train_ds,
        eval_dataset = val_ds,
        tokenizer=tokenizer,
        callbacks=[EarlyStoppingCallback(early_stopping_patience=5)], # 조기 종료
    )

    return trainer

# 모델 저장
def save_model(model, tokenizer, args):
    save_path = './wts/'
    os.makedirs(save_path, exist_ok=True)
    save_directory = save_path + "fine_tuned_" + f"{args.modelType}{args.mver}_{args.target}_lr{args.lr}_bs{args.batchsize}_numlabels{args.num_labels}"
    if os.path.exists(save_directory):
        idx = 0
        while(os.path.exists(save_directory)):
            idx += 1
            save_directory = save_path + f'fine_tuned_' + f"{args.modelType}{args.mver}_{args.target}_lr{args.lr}_bs{args.batchsize}_numlabels{args.num_labels}_" + str(idx)
        print(f'{save_directory} created!')
    
    model.save_pretrained(save_directory)
    tokenizer.save_pretrained(save_directory)

###########################################################
##################### 데이터 예측 함수 #####################
###########################################################
def predict(text, model, tokenizer, device):
    # 입력 문장 토크나이징
    inputs = tokenizer(text, return_tensors="pt", truncation=True, padding=True)
    inputs = {key: value.to(device) for key, value in inputs.items()}  # 각 텐서를 GPU로 이동

    # 모델 예측
    with torch.no_grad():
        outputs = model(**inputs)

    # 로짓을 소프트맥스로 변환하여 확률 계산
    logits = outputs.logits
    probabilities = logits.softmax(dim=1)

    # 가장 높은 확률을 가진 클래스 선택
    pred = torch.argmax(probabilities, dim=-1).item()

    return pred, probabilities

def predict_txts(txts, model, tokenizer, device):
    model.to(device)
    preds = []
    probs = []
    for txt in txts:
        pred, prob = predict(txt, model, tokenizer, device)
        preds.append(pred)
        probs.append(prob)

    return preds, probs






###########################################################
######################## EVALUATION #######################
###########################################################
def load_data_test(tokenizer, args):
    if args.target == 'abuse':
        url = './korean_immoral_texts_test.csv'
        data_ = pd.read_csv(url, sep=',')
        data_['abuse'] = ['ABS' if 'ABUSE' in datatypes else 'NON' for datatypes in data_['types'] ]
        data_.rename(columns={"sentences": "text", "abuse": "label"}, inplace=True)
        data_['label'] = data_['label'].map({'NON': 0, 'ABS': 1})
        
        # 데이터 불균형 해소
        data0 = data_[data_['label'] == 0]
        data1 = data_[data_['label'] == 1]
        data0_downsampled = resample(data0, replace=False, n_samples=len(data1), random_state=42)
        data = pd.concat([data0_downsampled, data1])

    elif args.target in ['immoral']:
        url = './korean_immoral_texts_test.csv'
        data = pd.read_csv(url, sep=',')
        data.rename(columns={"text": "text", "is_immoral": "label"}, inplace=True)
        data['label'] = data['label'].map({True: 0, False: 1})
    else: raise NotImplementedError('Not implemented yet')

    te_set = Dataset.from_pandas(data)

    def preprocess_function(data):
        return tokenizer(data['text'], truncation=True, padding=True, 
                         pad_to_max_length=True, max_length=128
                         )
    
    test_ds = te_set.map(preprocess_function, batched=True)
    return test_ds

def collate_fn(batch):
    input_ids = torch.tensor([item['input_ids'] for item in batch])
    attention_mask = torch.tensor([item['attention_mask'] for item in batch])
    labels = torch.tensor([item['label'] for item in batch])
    return {'input_ids': input_ids, 'attention_mask': attention_mask, 'label': labels}

def eval(model, tokenizer, device, args):
    test_ds = load_data_test(tokenizer, args)
    testloader = DataLoader(test_ds, batch_size=1, shuffle=False, collate_fn=collate_fn)

    model = model.to(device)
    model.eval()

    correct = 0
    total = 0

    preds = []
    labels_ = []
    with torch.no_grad():  # 평가 과정에서 기울기 계산 비활성화
        for batch in tqdm(testloader, total=len(testloader), ascii=' ='):
            input_ids = batch['input_ids'].to(device)
            # token_type_ids = torch.tensor(batch['token_type_ids'], device=device)
            attention_mask = torch.tensor(batch['attention_mask'], device=device)
            labels = batch['label'].to(device)
            outputs = model(input_ids=input_ids, attention_mask = attention_mask)

            _, predicted = torch.max(outputs.logits, 1)
            total += labels.size(0)
            correct += (predicted == labels).sum().item()

            preds.append(predicted.detach().cpu().numpy())
            labels_.append(labels.cpu().numpy())
    
    accuracy = correct / total
    print(f'Accuracy: {accuracy:.4f}')
    # print(f'Precision: {precision_score(labels_, preds):.4f}')
    # print(f'Recall: {recall_score(labels_, preds):.4f}')
    print(classification_report(labels_, preds, digits=4))
    print(confusion_matrix(labels_, preds))

# Main 함수
if __name__ == "__main__":

    args = call_args()
    device = torch.device("cuda" if torch.cuda.is_available else "cpu")

    # 학습일 때
    if args.mode == 'train':
        print("Load Model and Tokenizer...")
        model, tokenizer = call_model(args, device)
        print("Done!")

        print("Load data...")
        train_ds, val_ds = load_data(tokenizer, args)
        print("Done!")

        print("Begin Training...")
        trainer = set_train(model, tokenizer, train_ds, val_ds, args)
        trainer.train()
        print("Training Done!")

        print("Save model...")
        save_model(model, tokenizer, args)
        print("Done!")

    # 추론일 때
    elif args.mode == 'eval':
        save_fp = args.save_fp + f'fine_tuned_' + f"{args.modelType}{args.mver}_{args.target}_lr{args.lr}_bs{args.batchsize}_numlabels{args.num_labels}"
        model = AutoModelForSequenceClassification.from_pretrained(save_fp)
        tokenizer = AutoTokenizer.from_pretrained(save_fp)
        eval(model, tokenizer, device, args)
        # extra_sample_txt = ['자네 옷차림이 그게 뭔가',
        #                     '이걸 회의자료라고 만들어왔어',
        #                     '됐어 꺼져',
        #                     '자네는 정말 도움이 1도 안되는구만',
        #                     '정말 잘했어. 그래 그거야.',
        #                     '개발자님 이거 오늘까지 되나요?',
        #                     '이걸 지금 코드라고 짜왔어요?',
        #                     '아냐 넌 지금 아무것도 이해하지 못했어',
        #                     '됐다 말을 말자',
        #                     '너 진짜 이 개념 이해하고 쓰는 거 맞아?',
        #                     '넌 이것도 못 알아듣냐 멍청한 새끼야',
        #                     '씨발 그게 무슨 말도 안 되는 소리야',
        #                     '넌 정말 최고야',
        #                     '아까 18페이지에서 pagination 기능 설명해주신거, 다시 설명해주실 수 있을까요?']
        # preds, probs = predict_txts(extra_sample_txt, model, tokenizer, device)
        # for txt, pred, prob in zip(extra_sample_txt, preds, probs):
        #     print(f"\n문장: {txt}")
        #     print(f'예측된 클래스: {pred}')
        #     print(f'클래스별 확률: {prob}\n')