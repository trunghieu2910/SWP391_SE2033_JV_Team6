import os
import shutil
import random
import glob
import cv2
import numpy as np
import pandas as pd
import xml.etree.ElementTree as ET
from PIL import Image
from tqdm import tqdm

# --- CẤU HÌNH ĐƯỜNG DẪN TƯƠNG ĐỐI TỪ THƯ MỤC GỐC ---
BASE_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
IMG_DIR = os.path.join(BASE_DIR, "JPEGImages")
XML_DIR = os.path.join(BASE_DIR, "Annotations")
CSV_PATH = os.path.join(BASE_DIR, "image_processing_audit", "image_complexity_classification.csv")
OUTPUT_BASE = os.path.join(BASE_DIR, "YOLO26n", "datasets_v2")
PREVIEW_DIR = os.path.join(OUTPUT_BASE, "previews")

# Tạo cấu trúc thư mục đầu ra
for split in ['train', 'val', 'test']:
    os.makedirs(os.path.join(OUTPUT_BASE, f"images/{split}"), exist_ok=True)
    os.makedirs(os.path.join(OUTPUT_BASE, f"labels/{split}"), exist_ok=True)
os.makedirs(PREVIEW_DIR, exist_ok=True)

# --- CÁC HÀM XỬ LÝ ---

def parse_crop_coords(crop_str):
    """Phân tích chuỗi 'x1,y1,x2,y2' từ CSV"""
    if pd.isna(crop_str) or not isinstance(crop_str, str):
        return None
    try:
        return [int(float(x.strip())) for x in crop_str.split(',')]
    except:
        return None

def clean_ultrasound_marker(pil_img):
    """
    V11 Morphological: Dùng Top-Hat + Black-Hat để bắt marker (bất kể trắng/đen).
    Sau đó Inpaint TELEA + Speckle Noise để che sẹo.
    """
    # 1. Chuyển PIL sang OpenCV
    cv_img = cv2.cvtColor(np.array(pil_img), cv2.COLOR_RGB2BGR)
    gray = cv2.cvtColor(cv_img, cv2.COLOR_BGR2GRAY)

    # 2. MORPHOLOGICAL FILTERING
    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))

    # Top-Hat: bắt chi tiết SÁNG siêu nhỏ (lõi dấu + trắng)
    tophat = cv2.morphologyEx(gray, cv2.MORPH_TOPHAT, kernel)
    # Black-Hat: bắt chi tiết TỐI siêu nhỏ (viền bóng đen của dấu +)
    blackhat = cv2.morphologyEx(gray, cv2.MORPH_BLACKHAT, kernel)

    # Gộp lại thành bản đồ dị vật
    combined_morph = cv2.add(tophat, blackhat)

    # Threshold thấp vì nền morph đã tối, chỉ dị vật mới sáng
    _, mask = cv2.threshold(combined_morph, 35, 255, cv2.THRESH_BINARY)

    # Phình nhẹ 1 lần để bao trọn viền
    dilate_kernel = np.ones((3, 3), np.uint8)
    mask_dilated = cv2.dilate(mask, dilate_kernel, iterations=1)

    # 3. INPAINT TELEA
    inpainted_img = cv2.inpaint(cv_img, mask_dilated, 3, cv2.INPAINT_TELEA)

    # 4. SPECKLE NOISE (che sẹo mờ)
    noise = np.zeros(inpainted_img.shape, np.int16)
    cv2.randn(noise, mean=0, stddev=8)
    noisy_inpainted = cv2.add(inpainted_img, noise, dtype=cv2.CV_8UC3)

    mask_3channel = cv2.cvtColor(mask_dilated, cv2.COLOR_GRAY2BGR) / 255.0
    final_healed = (noisy_inpainted * mask_3channel + inpainted_img * (1 - mask_3channel)).astype(np.uint8)

    # 5. ĐỒNG BỘ MÀU SẮC (Grayscale)
    gray_final = cv2.cvtColor(final_healed, cv2.COLOR_BGR2GRAY)
    final_rgb = cv2.cvtColor(gray_final, cv2.COLOR_GRAY2RGB)

    return Image.fromarray(final_rgb)

def process_single_image(img_path, xml_path, group, crop_coords, split_folder, generate_preview=False):
    try:
        # 1. Đọc và kiểm tra ảnh
        img = Image.open(img_path)
        img.verify()
        img = Image.open(img_path).convert('RGB')
        orig_width, orig_height = img.size
        
        filename = os.path.basename(img_path)
        txt_filename = filename.replace('.jpg', '.txt').replace('.png', '.txt')
        new_img_path = os.path.join(OUTPUT_BASE, f"images/{split_folder}", filename)
        new_txt_path = os.path.join(OUTPUT_BASE, f"labels/{split_folder}", txt_filename)

        # 2. Thực hiện Crop (Tất cả các nhóm)
        if crop_coords and len(crop_coords) == 4:
            c_x1, c_y1, c_x2, c_y2 = crop_coords
        else:
            c_x1, c_y1, c_x2, c_y2 = 0, 0, orig_width, orig_height
            
        img_cropped = img.crop((c_x1, c_y1, c_x2, c_y2))
        new_width = c_x2 - c_x1
        new_height = c_y2 - c_y1

        # 3. Xóa Marker & Chuyển Grayscale (Tất cả các nhóm)
        img_final = clean_ultrasound_marker(img_cropped)

        # 4. Xử lý Annotation & Label YOLO
        yolo_labels = []
        preview_bboxes = []
        
        # Nhóm D là negative sample (label rỗng)
        if group != 'D' and xml_path and os.path.exists(xml_path):
            tree = ET.parse(xml_path)
            root = tree.getroot()
            
            for obj in root.findall('object'):
                bndbox = obj.find('bndbox')
                xmin_old = float(bndbox.find('xmin').text)
                ymin_old = float(bndbox.find('ymin').text)
                xmax_old = float(bndbox.find('xmax').text)
                ymax_old = float(bndbox.find('ymax').text)

                # Tịnh tiến theo crop
                xmin_new = xmin_old - c_x1
                ymin_new = ymin_old - c_y1
                xmax_new = xmax_old - c_x1
                ymax_new = ymax_old - c_y1

                # Loại bỏ nếu bbox bị crop ra ngoài hoàn toàn
                if xmin_new >= new_width or ymin_new >= new_height or xmax_new <= 0 or ymax_new <= 0:
                    continue

                # Kẹp giới hạn (Boundary check)
                xmin_new = max(0, xmin_new)
                ymin_new = max(0, ymin_new)
                xmax_new = min(new_width, xmax_new)
                ymax_new = min(new_height, ymax_new)
                
                # Check kích thước bbox hợp lệ tối thiểu
                if (xmax_new - xmin_new) < 10 or (ymax_new - ymin_new) < 10:
                    continue
                    
                preview_bboxes.append((int(xmin_new), int(ymin_new), int(xmax_new), int(ymax_new)))

                # YOLO format: class_id x_center y_center w h (chuẩn hóa 0-1)
                x_center = ((xmin_new + xmax_new) / 2) / new_width
                y_center = ((ymin_new + ymax_new) / 2) / new_height
                w_yolo = (xmax_new - xmin_new) / new_width
                h_yolo = (ymax_new - ymin_new) / new_height

                class_id = 0 # Myoma
                yolo_labels.append(f"{class_id} {x_center:.6f} {y_center:.6f} {w_yolo:.6f} {h_yolo:.6f}\n")

        # Ghi file label (Dù nhóm D thì ghi mảng rỗng sẽ thành file trống)
        with open(new_txt_path, 'w') as f_txt:
            f_txt.writelines(yolo_labels)

        # Lưu ảnh sạch
        img_final.save(new_img_path, quality=95)
        
        # Vẽ preview (Optional)
        if generate_preview:
            preview_img = np.array(img_final)
            for (px1, py1, px2, py2) in preview_bboxes:
                cv2.rectangle(preview_img, (px1, py1), (px2, py2), (255, 0, 0), 2)
            cv2.putText(preview_img, f"Grp:{group} Bbox:{len(preview_bboxes)}", (10, 30), cv2.FONT_HERSHEY_SIMPLEX, 1, (255, 0, 0), 2)
            Image.fromarray(preview_img).save(os.path.join(PREVIEW_DIR, f"prev_{filename}"))

        return True

    except Exception as e:
        print(f"Lỗi xử lý {filename}: {str(e)}")
        return False

# --- HÀM CHÍNH ---
def main():
    print("🚀 Bắt đầu quá trình Xóa Marker, Chuyển Grayscale và Chia Dataset (Stratified)...")
    
    # 1. Đọc CSV
    if not os.path.exists(CSV_PATH):
        print(f"Lỗi: Không tìm thấy file CSV tại {CSV_PATH}")
        return
        
    df = pd.read_csv(CSV_PATH)
    
    # 2. Phân loại ảnh theo nhóm
    groups_data = {'A': [], 'B': [], 'C': [], 'D': []}
    
    for idx, row in df.iterrows():
        filename = row['filename']
        cls = str(row['class'])
        crop_coords = parse_crop_coords(row['candidate_crop'])
        
        img_path = os.path.join(IMG_DIR, filename)
        xml_path = os.path.join(XML_DIR, filename.replace('.jpg', '.xml').replace('.png', '.xml'))
        
        if not os.path.exists(img_path):
            continue
            
        group = 'Unknown'
        if 'A_' in cls: group = 'A'
        elif 'B_' in cls: group = 'B'
        elif 'C_' in cls: group = 'C'
        elif 'D_' in cls: group = 'D'
        
        if group in groups_data:
            groups_data[group].append({
                'img_path': img_path,
                'xml_path': xml_path,
                'crop_coords': crop_coords,
                'group': group,
                'filename': filename
            })

    # 3. Phân chia Stratified Split (80/10/10) cho MỖI NHÓM ĐỘC LẬP
    train_data = []
    val_data = []
    test_data = []
    
    random.seed(42) # Cố định kết quả chia
    
    for g, items in groups_data.items():
        if not items: continue
        random.shuffle(items)
        
        total = len(items)
        
        # Sửa lỗi làm tròn: đảm bảo luôn có ít nhất 1 ảnh cho val và 1 cho test nếu total >= 3
        if total >= 3:
            n_val = max(1, int(total * 0.1))
            n_test = max(1, int(total * 0.1))
            n_train = total - n_val - n_test
        else:
            n_train = int(total * 0.8)
            n_val = int(total * 0.1)
            
        train_data.extend(items[:n_train])
        val_data.extend(items[n_train:n_train+n_val])
        test_data.extend(items[n_train+n_val:])
        
        print(f"Nhóm {g}: {total} ảnh -> Train:{n_train} | Val:{n_val} | Test:{total - n_train - n_val}")

    total_train, total_val, total_test = len(train_data), len(val_data), len(test_data)
    print(f"\nTổng cộng: Train:{total_train} | Val:{total_val} | Test:{total_test} (Tổng: {total_train+total_val+total_test})")

    # Hàm tiện ích xử lý batch
    def process_batch(data_list, split_name):
        success_count = 0
        preview_count = 0
        for i, item in enumerate(tqdm(data_list, desc=f"Processing {split_name}")):
            # Sinh preview cho 5 ảnh đầu mỗi tập
            gen_prev = (preview_count < 5)
            if process_single_image(item['img_path'], item['xml_path'], item['group'], item['crop_coords'], split_name, generate_preview=gen_prev):
                success_count += 1
                if gen_prev: preview_count += 1
        return success_count

    # 4. Thực thi xử lý
    print("\n--- Đang xử lý tập TRAIN ---")
    c_train = process_batch(train_data, 'train')
    
    print("\n--- Đang xử lý tập VAL ---")
    c_val = process_batch(val_data, 'val')
    
    print("\n--- Đang xử lý tập TEST ---")
    c_test = process_batch(test_data, 'test')

    print("\n==================================")
    print(f"🎉 HOÀN TẤT QUY TRÌNH!")
    print(f"📚 Tập Train : {c_train}/{total_train} ảnh")
    print(f"⚙️ Tập Val   : {c_val}/{total_val} ảnh")
    print(f"🎯 Tập Test  : {c_test}/{total_test} ảnh")
    print(f"📍 Dữ liệu sạch đã lưu tại: {OUTPUT_BASE}")
    print("==================================")

if __name__ == "__main__":
    main()
