import os, shutil
import cv2
import numpy as np
import time
import argparse
import glob
import face_recognition as fr
from tqdm import tqdm


def call_args():
    parser = argparse.ArgumentParser(description="WebCam Image auto saving")
    parser.add_argument('--fp', help='file path', default='./data/custom')
    parser.add_argument('--name', help='Input your name', default='yun')
    parser.add_argument('--num', help='maximum number of images to extract', type=int, default=1000)
    parser.add_argument('--mode', help='both: create&delete, del: delete only, other: preprocess', default='both', choices=['create', 'both', 'del', 'merge', 'label', 'video2frame'])
    args = parser.parse_args()
    return args

def create_dirs(args):
    if not os.path.exists(args.fp):
        os.makedirs(args.fp, exist_ok=True)
    ori_path = f'{args.fp}/ori'
    bbox_path = f'{args.fp}/bboxed'
    annot_path = f'{args.fp}/annot'
    os.makedirs(ori_path, exist_ok=True)
    os.makedirs(bbox_path, exist_ok=True)
    os.makedirs(annot_path, exist_ok=True)
    return (ori_path, bbox_path, annot_path)

def check_lastNum(fp):
    # 기존 촬영분이 있다면, 넘버링 이어받기.
    ## 없다면 0부터 넘버링
    if len(os.listdir(f'{fp}/ori')) == 0:
        last_num = -1
    else:
        nums = [int(num.split('_')[-1].split('.jp')[0]) for num in os.listdir(f'{fp}/ori')]
        last_num = np.max(nums)
    return last_num

def capture_owner_images(num_images):
    # 기존 촬영분 확인
    last_num = check_lastNum(args.fp)
    
    # 기본 웹캠 (0)으로 녹화 시작
    cap = cv2.VideoCapture(0)
    # cap = cv2.VideoCapture(cv2.CAP_ANY)

    img_count = 0
    color = (0, 255, 0)
    for _ in tqdm(range(num_images), desc='IMG', leave=False, ascii=' ='):
        _, frame = cap.read()
        frame = cv2.flip(frame, 1)
        frame_ori = frame.copy()
        frame_h, frame_w = frame.shape[0], frame.shape[1]

        ## 변환된 프레임에서 얼굴 (복수 가능) 탐지 시도
        faces = fr.face_locations(frame, number_of_times_to_upsample=2)
        idx = 0
        x_, y_, w_, h_ = [], [], [], []

        ## 탐지된 얼굴(들)의 좌표를 가져오는 반복문
        ### x_, y_: 바운딩박스의 중심점 좌표, w_, h_: 너비와 높이
        for (top, right, bottom, left) in faces:
            x_.append( f'{( (left+right)/2/frame_w ):.8f}' )
            y_.append( f'{( (top+bottom)/2/frame_h ):.8f}' )
            w_.append( f'{( (right-left)/frame_w ):.8f}')
            h_.append( f'{( (bottom-top)/frame_h ):.8f}')

            ### 얼굴 영역 표시
            cv2.rectangle(frame, (left,top), (right, bottom), color, 2)
            cv2.imshow('Captured Face', frame)


        # 바운딩박스 정보 텍스트화
        ## 가장 큰 바운딩박스만 저장
        str_ = []
        for i in range(len(x_)):
            if (w_[i] == sorted(w_, reverse=True)[0]) and (h_[i] == sorted(h_, reverse=True)[0]):
                str_.append(f'{idx} {x_[i]} {y_[i]} {w_[i]} {h_[i]}')

        # 얼굴 영역의 바운딩박스가 하나만 존재할 경우, 얼굴 영역 표시된 이미지 저장
        if len(x_) == 1:
            ## 몇장 저장되는지 카운트
            
            captured_file = f"{args.fp}/bboxed/{args.name}_{img_count + last_num + 1}.jpg"
            cv2.imwrite(captured_file, frame)
            with open(f'{args.fp}/annot/{args.name}_{img_count + last_num + 1}.txt', 'w+') as f:
                f.write('\n'.join(str_))
            
            ## 원본 및 Bounding-box 정보, 결과물 저장
            original_file = f'{args.fp}/ori/{args.name}_{img_count + last_num + 1}.jpg'
            cv2.imwrite(original_file, frame_ori)

            img_count += 1

        # 아래 코드 없으면 화면에 표시가 안됨
        if cv2.waitKey(1) & 0xFF == ord('q'):
            break
    
    cap.release()
    cv2.destroyAllWindows()

    return img_count

def capture_noOwner_images(num_images):
    # 기존 촬영분 확인
    last_num = check_lastNum(args.fp)

    # 기본 웹캠 (0)으로 녹화 시작
    cap = cv2.VideoCapture(0)

    img_count = 0
    for _ in tqdm(range(num_images), desc='IMG', leave=False, ascii=' ='):
        _, frame = cap.read()
        frame = cv2.flip(frame, 1)

        ## 원본 및 Bounding-box 정보, 결과물 저장
        original_file = f'{args.fp}/ori/{args.name}_{img_count + last_num + 1}.jpg'
        cv2.imwrite(original_file, frame)
        with open(f'{args.fp}/annot/{args.name}_{img_count + last_num + 1}.txt', 'w+') as f: pass
        img_count += 1

    cap.release()
    cv2.destroyAllWindows()

    return img_count
        
def modify_annot(args):
    path = args.fp + '/other'
    db_list = os.listdir(path)

    for db in tqdm(db_list, position=0):
        print(f'Modifying labels in {db}')
        labels_fp_list = glob.glob(f'{path}/{db}/annot/*.txt')

        for labels_fp in tqdm(labels_fp_list, position=1, leave=False):
            new_lines = []
            with open(labels_fp, 'r') as f:
                lines = f.readlines()
            for i, line in enumerate(lines):
                elements = line.split(' ')
                elements[0] = '1'
                new_lines.append( ' '.join(elements) )

            filename = os.path.basename(labels_fp)
            os.makedirs(f'{path}/{db}/annot_edit', exist_ok=True)
            with open(f'{path}/{db}/annot_edit/{filename}', 'w') as f: pass

        shutil.rmtree(f'{path}/{db}/annot')
        os.rename(src=f'{path}/{db}/annot_edit', dst=f'{path}/{db}/annot')

def modify_others(args):
    path = args.fp + '/other'
    data_list = os.listdir(path)

    our_members = ['onion', 'yerim']
    diff_members = ['yeonghyun', 'byeongjin', 'gwangha']

    for folder_name in tqdm(data_list, position=0):
        name = folder_name.split('_')[0]
        
        if name in our_members:
            ori_path = path + f'/{folder_name}/ori'

            if os.listdir(ori_path)[0].split('_')[0] == 'yun':
                filenames = glob.glob(path + f'/{folder_name}/*/*')

                for file in tqdm(filenames, position=1, leave=False):
                    ftype = file.split('\\')[-2]
                    basename_num = os.path.basename(file).split('_')[1]
                    basename = f'{name}_{basename_num}'
                    os.rename(src=file, dst=f'{path}/{folder_name}/{ftype}/{basename}')

        elif name in diff_members:
            # annot과 ori가 있으나 파일명 중복이 의심되는 경우
            ori_path = path + f'/{folder_name}/ori'

            if os.listdir(ori_path)[0].split('_')[0] != name:
                filenames = glob.glob(path + f'/{folder_name}/*/*')

                for file in tqdm(filenames, position=1, leave=False):
                    ftype = file.split('\\')[-2]
                    basename= os.path.basename(file)
                    basename = f'{name}_{basename}'
                    os.rename(src=file, dst=f'{path}/{folder_name}/{ftype}/{basename}')

def del_noTxt(paths):
    ori_path, bbox_path, annot_path = paths
    img_fp_list = os.listdir(ori_path)
    bboxImg_fp_list = os.listdir(bbox_path)
    # annot_fp_list = os.listdir(annot_path)
    
    for file_name in tqdm(img_fp_list):
        if file_name not in bboxImg_fp_list:
            if os.path.exists(f'{ori_path}/{file_name}'): os.remove(f'{ori_path}/{file_name}')
            if os.path.exists(f'{annot_path}/{file_name.split('.jp')[0]}.txt'): os.remove(f'{annot_path}/{file_name.split('.jp')[0]}.txt')

def mod_label():
    names = [
        'onion', 'yun', 'suhyeon', 'seung', 
        'lyr', 'jihong']
    labels_text = ['normal', 'tired']

    for name in tqdm(names):
        for label_text in labels_text:
            print(f'Modifying labels in ./data/custom/{name}_{label_text}/annot')

            labels_fp_list = glob.glob(f'./data/custom/{name}_{label_text}/annot/*.txt')

            if label_text == 'tired':
                label = '1'
            else:
                label = '0'

            os.makedirs(f'./data/custom/Datasets/total/images', exist_ok=True)
            os.makedirs(f'./data/custom/Datasets/total/labels', exist_ok=True)

            for labels_fp in tqdm(labels_fp_list):
                new_lines = []
                with open(labels_fp, 'r') as f:
                    lines = f.readlines()
                    for _, line in enumerate(lines):
                        elements = line.split(' ')
                        elements[0] = label
                        new_lines.append( ' '.join(elements) )

                labels_fp_split = os.path.basename(labels_fp)
                num = os.path.splitext(labels_fp_split)[0].split('_')[-1]
                
                with open(f'./data/custom/Datasets/total/labels/{name}_{label_text}_{num}.txt', 'w') as f:
                    f.write(''.join(new_lines))
                
                if name == 'suhyeon':
                    if label_text == 'normal':
                        shutil.copy(f'./data/custom/{name}_{label_text}/ori/{name}_nottired_{num}.jpg', 
                                f'./data/custom/Datasets/total/images/{name}_{label_text}_{num}.jpg')
                    else:
                        shutil.copy(f'./data/custom/{name}_{label_text}/ori/{name}_{label_text}_{num}.jpg', 
                                    f'./data/custom/Datasets/total/images/{name}_{label_text}_{num}.jpg')
                elif name == 'lyr':
                    if label_text == 'normal':
                        shutil.copy(f'./data/custom/{name}_{label_text}/ori/{name}_{num}.jpg', 
                                f'./data/custom/Datasets/total/images/{name}_{label_text}_{num}.jpg')
                    else:
                        shutil.copy(f'./data/custom/{name}_{label_text}/ori/{name}_{label_text}_{num}.jpg', 
                                    f'./data/custom/Datasets/total/images/{name}_{label_text}_{num}.jpg')
                else:
                    shutil.copy(f'./data/custom/{name}_{label_text}/ori/{name}_{num}.jpg', 
                                f'./data/custom/Datasets/total/images/{name}_{label_text}_{num}.jpg')

def split_frames():
    filepaths = ['./data/custom/jihong_normal.mp4', './data/custom/jihong_tired.mp4']
    color = (0, 255, 0)

    img_counts = [0,0]
    for ii, fp in enumerate(filepaths):
        cap = cv2.VideoCapture(fp)
        fps = cap.get(cv2.CAP_PROP_FPS)
        frame_h, frame_w = cap.get(cv2.CAP_PROP_FRAME_HEIGHT), cap.get(cv2.CAP_PROP_FRAME_WIDTH)
        frame_length = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))

        if not cap.isOpened():
            print("Could not open: ", fp)
            exit(0)

        dir_name = fp.split('.mp')[0]
        os.makedirs(dir_name, exist_ok=True)
        os.makedirs(dir_name + '/ori/', exist_ok=True)
        os.makedirs(dir_name + '/annot/', exist_ok=True)
        os.makedirs(dir_name + '/bboxed/', exist_ok=True)
        last_num = -1
        
        for frame_idx in tqdm(range(frame_length)):
            if frame_idx % 9 == 0:
                _, frame = cap.read()
                # frame = cv2.flip(frame, 1)

                frame_ori = frame.copy()
                frame_h, frame_w = frame.shape[0], frame.shape[1]
                print(frame_h, frame_w)

                ## 변환된 프레임에서 얼굴 (복수 가능) 탐지 시도
                faces = fr.face_locations(frame, number_of_times_to_upsample=2)
                idx = 0
                x_, y_, w_, h_ = [], [], [], []

                ## 탐지된 얼굴(들)의 좌표를 가져오는 반복문
                ### x_, y_: 바운딩박스의 중심점 좌표, w_, h_: 너비와 높이
                for (top, right, bottom, left) in faces:
                    x_.append( f'{( (left+right)/2/frame_w ):.8f}' )
                    y_.append( f'{( (top+bottom)/2/frame_h ):.8f}' )
                    w_.append( f'{( (right-left)/frame_w ):.8f}')
                    h_.append( f'{( (bottom-top)/frame_h ):.8f}')

                    ### 얼굴 영역 표시
                    cv2.rectangle(frame, (left,top), (right, bottom), color, 2)
                    cv2.imshow('Captured Face', frame)

                # 바운딩박스 정보 텍스트화
                ## 가장 큰 바운딩박스만 저장
                str_ = []
                for i in range(len(x_)):
                    if (w_[i] == sorted(w_, reverse=True)[0]) and (h_[i] == sorted(h_, reverse=True)[0]):
                        str_.append(f'{idx} {x_[i]} {y_[i]} {w_[i]} {h_[i]}')

                # 얼굴 영역의 바운딩박스가 하나만 존재할 경우, 얼굴 영역 표시된 이미지 저장
                if len(x_) == 1:
                    ## 몇장 저장되는지 카운트
                    
                    captured_file = f"{dir_name}/bboxed/{args.name}_{img_counts[ii] + last_num + 1}.jpg"
                    cv2.imwrite(captured_file, frame)
                    with open(f'{dir_name}/annot/{args.name}_{img_counts[ii] + last_num + 1}.txt', 'w+') as f:
                        f.write('\n'.join(str_))
                    
                    ## 원본 및 Bounding-box 정보, 결과물 저장
                    original_file = f'{dir_name}/ori/{args.name}_{img_counts[ii] + last_num + 1}.jpg'
                    cv2.imwrite(original_file, frame_ori)

                    img_counts[ii] += 1

                # 아래 코드 없으면 화면에 표시가 안됨
                if cv2.waitKey(1) & 0xFF == ord('q'):
                    break
        
        cap.release()
        cv2.destroyAllWindows()

    return img_counts


if __name__ == '__main__':
    args = call_args()
    paths = create_dirs(args)

    # 얼굴 데이터 취득용
    # 얼굴 미포함 데이터 취득 시 create로 빈 어노테이션 파일 생성되게끔
    if args.mode == 'both':
        print('데이터 취득 중...')
        img_count = capture_owner_images(args.num)
        print(f'완료! \n총 {img_count}장 생성되었습니다!')
    elif args.mode == 'create':
        print('데이터 취득 중...')
        img_count = capture_noOwner_images(args.num)
        print(f'완료! \n총 {img_count}장 생성되었습니다!')
    
    # 예외처리
    if args.mode in ['both', 'del']:
        del_noTxt(paths)

    # 이름 변경용
    if args.mode == 'merge':
        print('다른 분들 데이터 파일명 수정 중...')
        modify_others(args)
        print('완료!')
        print('다른 분들 데이터 annotation 수정 중...')
        modify_annot(args)
        print('완료!')

    if args.mode == 'label':
        mod_label()

    if args.mode == 'video2frame':
        print('동영상에서 데이터 취득 중...')
        img_counts = split_frames()
        print(f'완료! \nNormal 총 {img_counts[0]}장 생성되었습니다!')
        print(f'Tired 총 {img_counts[0]}장 생성되었습니다!')