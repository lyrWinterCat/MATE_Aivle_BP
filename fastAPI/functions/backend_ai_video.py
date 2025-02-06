import os
import numpy as np
import cv2
from ultralytics import YOLO
import numpy as np

def load_crop_img(img_path):
    img = cv2.imread(img_path)
    imgs = define_strategy(img)
    return imgs

def define_strategy(img):
    if img.shape[0] == 804:
        # img_ = cv2.cvtColor(img[242:346, 1826:1912, :], cv2.COLOR_BGR2RGB)
        img_ = cv2.cvtColor(img[149:797, 1886:1919, :], cv2.COLOR_BGR2RGB)

        if np.amin(img_) == np.amax(img_):
            return img_area1(img)
        else:
            return img_area2(img)
    elif img.shape[0] == 892:
        img_ = cv2.cvtColor(img[479:565, 1856:1912, :], cv2.COLOR_BGR2RGB)

        if np.amin(img_) == np.amax(img_):
            return img_area4(img)
        else:
            return img_area3(img)
    else:
        raise NotImplementedError('Not supported image size')

# 화면 공유 X
def img_area1(img):
    '''
    img: ndarray 형태
    '''
    if isinstance(img, np.ndarray): pass
    else: raise NotImplementedError('Ndarray supported only')
        
    # 공유화면 X 기준
    # 알림창 있는 경우는 주석 처리
    # h = 304
    # w = 544
    # h1, w1 = 184, 180
    h = 326
    w = 584
    d = 5
    h1, w1 = 140, 121

    pts = []
    # 사람 1 영역
    pts.append((h1, w1))
    pts.append((h1 + h, w1 + w))
    # 사람 2 영역
    pts.append((h1, w1 + w + d))
    pts.append((h1 + h, w1 + 2*w + d))
    # 사람 3 영역
    pts.append((h1, w1 + 2*w + 2*d))
    pts.append((h1 + h, w1 + 3*w + 2*d))
    # 사람 4 영역
    pts.append((h1 + h + d, w1))
    pts.append((h1 + 2*h + d, w1 + w))
    # 사람 5 영역
    pts.append((h1 + h + d, w1 + w + d))
    pts.append((h1 + 2*h + d, w1 + 2*w + d))
    # 사람 6 영역
    pts.append((h1 + h + d, w1 + 2*w + 2*d))
    pts.append((h1 + 2*h + d, w1 + 3*w + 2*d))

    imgs = []
    for i in range(6):
        imgs.append(img[pts[2*i][0]:(pts[2*i+1][0]+1), pts[2*i][1]:(pts[2*i+1][1]+1), :])
    return imgs, 0
        

def img_area2(img):
    '''
    img: ndarray 형태
    '''
    if isinstance(img, np.ndarray): pass
    else: raise NotImplementedError('Ndarray supported only')

    h = 107
    w = 145
    # h1, w1 = 240, 1618
    h1, w1 = 218, 1619
    d = 5

    pts = []
    # 사람 1 영역
    pts.append((h1, w1))
    pts.append((h1 + h, w1 + w))
    # 사람 2 영역
    pts.append((h1, w1 + w + d))
    pts.append((h1 + h, w1 + 2*w + d))
    # 사람 3 영역
    pts.append((h1 + h + d, w1))
    pts.append((h1 + 2*h + d, w1 + w))
    # 사람 4 영역
    pts.append((h1 + h + d, w1 + w + d))
    pts.append((h1 + 2*h + d, w1 + 2*w + d))
    # 사람 5 영역
    # pts.append((465, 1693))
    # pts.append((572, 1838))
    pts.append((443, 1694))
    pts.append((550, 1839))
    # 사람 6 영역
    pts.append((556, 1619))
    pts.append((720, 1914))

    imgs = []
    for i in range(6):
        imgs.append(img[pts[2*i][0]:(pts[2*i+1][0]+1), pts[2*i][1]:(pts[2*i+1][1]+1), :])
    return imgs, 1

# 화면 공유 X
def img_area3(img):
    '''
    img: ndarray 형태
    '''
    if isinstance(img, np.ndarray): pass
    else: raise NotImplementedError('Ndarray supported only')
        
    # 공유화면 X 기준
    h = 341
    w = 610
    d = 4

    h1, w1 = 156, 73
    pts = []
    # 사람 1 영역
    pts.append((h1, w1))
    pts.append((h1 + h, w1 + w))
    # 사람 2 영역
    pts.append((h1, w1 + w + d))
    pts.append((h1 + h, w1 + 2*w + d))
    # 사람 3 영역
    pts.append((h1, w1 + 2*w + 2*d))
    pts.append((h1 + h, w1 + 3*w + 2*d))
    # 사람 4 영역
    pts.append((h1 + h + d, w1))
    pts.append((h1 + 2*h + d, w1 + w))
    # 사람 5 영역
    pts.append((h1 + h + d, w1 + w + d))
    pts.append((h1 + 2*h + d, w1 + 2*w + d))
    # 사람 6 영역
    pts.append((h1 + h + d, w1 + 2*w + 2*d))
    pts.append((h1 + 2*h + d, w1 + 3*w + 2*d))

    imgs = []
    for i in range(6):
        imgs.append(img[pts[2*i][0]:(pts[2*i+1][0]+1), pts[2*i][1]:(pts[2*i+1][1]+1), :])
    return imgs, 2


# 화면공유 O
def img_area4(img):
    '''
    img: ndarray 형태
    '''
    if isinstance(img, np.ndarray): pass
    else: raise NotImplementedError('Ndarray supported only')

    h = 86
    w = 116
    d = 4

    h1, w1 = 299, 1677
    pts = []
    # 사람 1 영역
    pts.append((h1, w1))
    pts.append((h1 + h, w1 + w))
    # 사람 2 영역
    pts.append((h1, w1 + w + d))
    pts.append((h1 + h, w1 + 2*w + d))
    # 사람 3 영역
    pts.append((h1 + h + d, w1))
    pts.append((h1 + 2*h + d, w1 + w))
    # 사람 4 영역
    pts.append((h1 + h + d, w1 + w + d))
    pts.append((h1 + 2*h + d, w1 + 2*w + d))
    # 사람 5 영역
    pts.append((479, 1737))
    pts.append((565, 1853))
    # 사람 6 영역
    pts.append((569, 1677))
    pts.append((700, 1912))

    imgs = []
    for i in range(6):
        imgs.append(img[pts[2*i][0]:(pts[2*i+1][0]+1), pts[2*i][1]:(pts[2*i+1][1]+1), :])
    return imgs, 3



def check_fatigue(images, mode):
    results_ = []
    results_imgs = []

    # 모든 이미지 크기가 동일한 경우 (공유화면 x)
    if mode in [0, 2]:
        img_size = 360

    elif mode in [1, 3]:
        img_size = 120

    model = YOLO(f'./wts/YOLO/model_YOLO11n_{img_size}_best.pt')
    
    results = model.predict(images, 
                            # save=True, save_txt=True, 
                            # line_width=2, 
                            iou=0.5, conf=0.5,
                            # iou=0.7, conf=0.25,
                            verbose=False)
    
    if len(results) != 0:
        for r in results:
            # r_b: 이미지 내의 바운딩 박스'들'의 정보를 가지고 있음
            r_b = r.boxes
            img = r.orig_img
            box_info = []
            # r_b.의 클래스 예측 값이 없는게 아니라면,
            if not r_b.cls == None:
                ## r_b가 클래스 예측한만큼 반복 수행
                for idx in range(len(r_b)):
                    
                    ### 바운딩박스의 점 2개 좌표
                    x1, y1, x2, y2 = int(r_b.xyxy[idx][0]), int(r_b.xyxy[idx][1]), int(r_b.xyxy[idx][2]), int(r_b.xyxy[idx][3])

                    ### 1. r_b에 대한 신뢰 점수가 임계값을 넘으면서,
                    ### 2. r_b의 클래스 예측이 1이라면 (즉, 피곤하다면) 빨간 박스
                    if r_b.cls[idx] == 1:
                        color = (0, 0, 255)
                        conf = r_b.conf[idx]*100
                        label_text = f'Fatigue : {conf:.2f}'
                        results_.append(r_b.cls[idx].cpu().numpy())

                    ### 1. r_b에 대한 신뢰 점수가 임계값을 넘으면서,
                    ### 2. r_b의 클래스 예측이 0이라면 (즉, 평소 상태라면) 초록 박스
                    else:
                        color = (0, 255, 0)
                        conf = r_b.conf[idx]*100
                        label_text = f'Normal : {conf:.2f}'
                        results_.append(r_b.cls[idx].cpu().numpy())
                    
                    ## 바운딩박스치기
                    if img.shape[0] != 305:
                        ratioH = 305/img.shape[0]
                        ratioW = 545/img.shape[1]
                        img = cv2.resize(img, (545, 305))
                        x1, y1, x2, y2 = int(x1*ratioW), int(y1*ratioH), int(x2*ratioW), int(y2*ratioH)

                    # 가장 큰 박스만 남기기 위해 모든 박스 데이터 저장
                    box_info.append((x1, y1, x2, y2, color, label_text))

                # 가장 큰 박스만 남겨서 표시
                max_size = 0
                for bi in box_info:
                    x1, y1, x2, y2, color, label_text = bi
                    if (x2-x1) * (y2-y1) > max_size:
                        max_size = (x2-x1) * (y2-y1)
                        x1m, y1m, x2m, y2m, cm, lm = x1, y1, x2, y2, color, label_text
                cv2.rectangle(img, (x1m, y1m), (x2m, y2m), cm, 2)
                cv2.putText(img, lm, (x1m, y1m - 10),
                            cv2.FONT_HERSHEY_SIMPLEX, 0.6, cm, 2)

            if img.shape[0] != 305:
                img = cv2.resize(img, (545, 305))

            results_imgs.append(img)

    results_imgs = np.vstack((np.hstack((results_imgs[0:3])), np.hstack((results_imgs[3:6]))))
    fatigue_ratio = np.mean(results_)
    results_ = ['normal' if result == 0 else 'tired' for result in results_]

    return fatigue_ratio, results_, results_imgs

if __name__ == '__main__':
    # img_path = 'data/video/temp_screenshot-2025-01-22T01_48_37.786Z.png' # Case 1
    # img_path = 'data/video/temp_screenshot-2025-01-22T01_47_39.256Z.png' # Case 2
    # img_path = 'data/video/temp_screenshot-2025-02-05T01_05_54.749Z.png'
    # img_path = 'data/video/temp_screenshot-2025-02-05T01_13_54.647Z.png'
    # img_path = 'data/video/temp_screenshot-2025-02-05T02_05_54.368Z.png'
    # img_path = 'data/video/temp_screenshot-2025-02-05T02_07_59.250Z.png'
    # img_path = 'data/video/이미지.png'
    img_path = 'data/video/이미지2.png'
    if not os.path.exists(img_path): raise FileNotFoundError('No file')

    imgs, mode = load_crop_img(img_path)
    # for img  in imgs:
    #     cv2.imshow('All players', img)
    #     cv2.waitKey(0)
    #     cv2.destroyAllWindows()

    fratio, _, results_imgs = check_fatigue(imgs, mode)
    print(f'피로 인원 비율: {fratio*100:.2f}%')
    cv2.imshow('All players', results_imgs)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
    