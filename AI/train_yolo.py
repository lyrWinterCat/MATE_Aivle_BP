import argparse, os, cv2, glob, shutil
import torch
import numpy as np
from legacy import scaling, conv2d_bn, _generate_layer_name, _inception_resnet_block
from ultralytics import YOLO, settings
from tqdm import tqdm
import random
import yaml

def call_args():
    parser = argparse.ArgumentParser(description='')
    parser.add_argument('--model_size', required=True, choices=['n', 's', 'l'], help='')
    parser.add_argument('--datapath', default='./data/custom/', help='working directory')
    parser.add_argument('--wtspath', default='./wts/YOLO/')
    parser.add_argument('--img_size', type=int, required=True, choices=[120, 240, 360, 480])
    args = parser.parse_args()
    return args

def load_model(args):
    model = YOLO(args.wtspath + f'model_YOLO11{args.model_size}_{args.img_size}_best.pt', task='detect')
    return model

def split_dataset(args):
    val_ratio = 0.2
    fp_list_w_jpg = glob.glob(args.datapath + 'Datasets/total/images/*.jpg')
    random.shuffle(fp_list_w_jpg)
    fp_list_w_txt = ['/'.join(jpg_fp.split('/')[:-2]) + '/total/labels/' + os.path.splitext(os.path.basename(jpg_fp))[0] + '.txt' for jpg_fp in fp_list_w_jpg]

    val_num = int(len(fp_list_w_jpg) * val_ratio)
    train_jpg_list = fp_list_w_jpg[val_num:]
    train_txt_list = fp_list_w_txt[val_num:]
    val_jpg_list = fp_list_w_jpg[:val_num]
    val_txt_list = fp_list_w_txt[:val_num]

    print('# trainset  :', len(train_jpg_list))
    print('# valset  :', len(val_jpg_list))

    os.makedirs('./data/custom/Datasets/YOLO/images/train', exist_ok=True)
    os.makedirs('./data/custom/Datasets/YOLO/images/val', exist_ok=True)
    os.makedirs('./data/custom/Datasets/YOLO/labels/train', exist_ok=True)
    os.makedirs('./data/custom/Datasets/YOLO/labels/val', exist_ok=True)

    print(f'Copying trainset ...')
    for i, train_jpg in tqdm(enumerate(train_jpg_list)):
        filename = os.path.splitext(os.path.basename(train_jpg))[0]
        if (filename == os.path.splitext(os.path.basename(train_txt_list[i]))[0]):
            shutil.copy(src=train_jpg, dst=f'./data/custom/Datasets/YOLO/images/train/{filename}.jpg')
            shutil.copy(src=train_txt_list[i], dst=f'./data/custom/Datasets/YOLO/labels/train/{filename}.txt')
    print('Done!')

    print(f'Copying valset ...')
    for i, val_jpg in tqdm(enumerate(val_jpg_list)):
        filename = os.path.splitext(os.path.basename(val_jpg))[0]
        if (filename == os.path.splitext(os.path.basename(val_txt_list[i]))[0]):
            shutil.copy(src=val_jpg, dst=f'./data/custom/Datasets/YOLO/images/val/{filename}.jpg')
            shutil.copy(src=val_txt_list[i], dst=f'./data/custom/Datasets/YOLO/labels/val/{filename}.txt')
    print('Done!')

############################################################################
############################################################################

def mk_yaml(args):
    yaml_ = {
        "path": f"{args.datapath}Datasets/YOLO/",
        "train": "images/train",
        "val": "images/val",
        "names": { 0: "normal",
                  1: "tired" }
    }
    os.makedirs("./data/custom/Datasets/YOLO", exist_ok=True)
    with open("./data/custom/Datasets/YOLO/data.yaml", "w") as f:
        yaml.dump(yaml_, f)

def train(args):
    yaml_dir = args.datapath + 'Datasets/YOLO/data.yaml'
    settings['datasets_dir'] = args.datapath

    model = YOLO('yolo11n.pt')
    imgsize = (args.img_size, int(640/(480/args.img_size)))
    results = model.train(data=yaml_dir, epochs=20, imgsz=imgsize, patience=5, pretrained=True)

    os.makedirs(args.wtspath, exist_ok=True)
    model.save(args.wtspath + f'model_YOLO11{args.model_size}_{args.img_size}_best.pt')

############################################################################
############################################################################


if __name__ == '__main__':
    args = call_args()

    if not os.path.exists(args.datapath + 'Datasets/YOLO/images/train'):
        print('Split dataset into train, validation sets...')
        split_dataset(args)
        print('Done!')

    if not os.path.exists(args.wtspath + f'model_YOLO11{args.model_size}_{args.img_size}_best.pt'):
        yaml_dir = args.datapath + 'Datasets/YOLO/data.yaml'
        if not os.path.exists(yaml_dir):
            mk_yaml(args)
        print('Start training...')
        train(args)
        print('Done!')
    else:
        print('Already trained with given hyperparameters')