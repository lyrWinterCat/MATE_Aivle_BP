import json
import pandas as pd
from tqdm import tqdm

# Case 1. AI_HUB 텍스트 윤리검증 데이터
fp = './147.텍스트 윤리검증 데이터/01.데이터/2.Validation/라벨링데이터/aihub/VL1_aihub/talksets-train-6/talksets-train-6.json'
texts = []
types = []
is_immoral = []

print('필요한 데이터 추출...')
with open(fp, 'r', encoding='utf-8') as f:
    json_data = json.load(f)

for i in tqdm(json_data, total=len(json_data), desc='JSON', ascii=' =', leave=False, position=1):
    for sentence in tqdm(i["sentences"], total=len(i["sentences"]), desc='SENTENCES', ascii=' =', leave=False, position=2):
        texts.append(sentence['origin_text'])
        types.append(', '.join(sentence['types'][:]))
        is_immoral.append(sentence['is_immoral'])
print('완료!')
    
df = pd.DataFrame({'text': texts, 'types': types, 'is_immoral': is_immoral})
df.to_csv("./korean_immoral_texts_test.csv", index=False)