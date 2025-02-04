import os
import numpy as np
import cv2
from ultralytics import YOLO
import numpy as np

def load_crop_img(img_path):
    img = cv2.imread(img_path)
    imgs = define_strategy(img)
    return imgs

def load_crop_img_by_bytes(bytes):
    image_array = np.frombuffer(bytes, dtype=np.uint8)
    
    img = cv2.imdecode(image_array, cv2.IMREAD_COLOR)
    
    imgs = define_strategy(img)
    return imgs

def define_strategy(img):
    img_ = cv2.cvtColor(img[242:346, 1826:1912, :], cv2.COLOR_BGR2RGB)

    if np.amin(img_) == np.amax(img_):
        return img_area1(img)
    else:
        return img_area2(img)
    

# 화면 공유 X
def img_area1(img):
    '''
    img: ndarray 형태
    '''
    if isinstance(img, np.ndarray): pass
    else: raise NotImplementedError('Ndarray supported only')
        
    # 공유화면 X 기준
    h = 304
    w = 544
    d = 5

    h1, w1 = 184, 180
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
    d = 5

    h1, w1 = 240, 1618
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
    pts.append((465, 1693))
    pts.append((572, 1838))
    # 사람 6 영역
    pts.append((577, 1618))
    pts.append((741, 1913))

    imgs = []
    for i in range(6):
        imgs.append(img[pts[2*i][0]:(pts[2*i+1][0]+1), pts[2*i][1]:(pts[2*i+1][1]+1), :])
    return imgs, 1


def check_fatigue(images, mode):
    results_ = []
    results_imgs = []

    # 모든 이미지 크기가 동일한 경우 (공유화면 x)
    if mode == 0:
        img_size = 360

    elif mode == 1:
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
            # r_b.의 클래스 예측 값이 없는게 아니라면,
            if not r_b.cls == None:
                ## r_b가 클래스 예측한만큼 반복 수행
                for idx in range(len(r_b)):
                    ### 바운딩박스의 점 2개 좌표
                    x1, y1, x2, y2 = int(r_b.xyxy[idx][0]), int(r_b.xyxy[idx][1]), int(r_b.xyxy[idx][2]), int(r_b.xyxy[idx][3])

                    ### 1. r_b에 대한 신뢰 점수가 임계값을 넘으면서,
                    ### 2. r_b의 클래스 예측이 1이라면 (즉, 피곤하다면)
                    if r_b.cls[idx] == 1:
                        color = (0, 0, 255)
                        conf = r_b.conf[idx]*100
                        label_text = f'Fatigue : {conf:.2f}'
                        results_.append(r_b.cls[idx].cpu().numpy())

                    ### 1. r_b에 대한 신뢰 점수가 임계값을 넘으면서,
                    ### 2. r_b의 클래스 예측이 0이라면 (즉, 평소 상태라면)
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

                    cv2.rectangle(img, (x1, y1), (x2, y2), color, 2)
                    cv2.putText(img, label_text, (x1, y1 - 10),
                                cv2.FONT_HERSHEY_SIMPLEX, 0.6, color, 2)

            if img.shape[0] != 305:
                img = cv2.resize(img, (545, 305))

            # if img.shape[0] not in [305, 108]:
            #     img = cv2.resize(img, (146, 108))
            results_imgs.append(img)

    results_imgs = np.vstack((np.hstack((results_imgs[0:3])), np.hstack((results_imgs[3:6]))))
    fatigue_ratio = np.mean(results_)
    results_ = ['normal' if result == 0 else 'tired' for result in results_]

    return fatigue_ratio, results_, results_imgs

if __name__ == '__main__':
    # img_path = 'data/video/temp_screenshot-2025-01-22T01_48_37.786Z.png' # Case 1
    img_path = 'data/video/temp_screenshot-2025-01-22T01_47_39.256Z.png' # Case 2
    if not os.path.exists(img_path): raise FileNotFoundError('No file')

    imgs, mode = load_crop_img(img_path)

    fratio, _, results_imgs = check_fatigue(imgs, mode)
    print(f'피로 인원 비율: {fratio*100:.2f}%')
    cv2.imshow('All players', results_imgs)
    cv2.waitKey(0)
    cv2.destroyAllWindows()
    