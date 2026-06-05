import os
import shutil

base_dir = r"d:\Hieu\Project_SWP391"
datasets_dir = os.path.join(base_dir, "YOLO26n", "datasets_v2", "images")
jpeg_dir = os.path.join(base_dir, "JPEGImages")
group_dir = os.path.join(base_dir, "GroupImages")

splits = ["train", "val", "test"]

for split in splits:
    target_split_dir = os.path.join(group_dir, split)
    os.makedirs(target_split_dir, exist_ok=True)
    
    source_split_dir = os.path.join(datasets_dir, split)
    if not os.path.exists(source_split_dir):
        continue
        
    filenames = os.listdir(source_split_dir)
    print(f"Copying {len(filenames)} raw images for {split} split...")
    
    for filename in filenames:
        if filename.endswith(".jpg") or filename.endswith(".png"):
            raw_img_path = os.path.join(jpeg_dir, filename)
            target_img_path = os.path.join(target_split_dir, filename)
            
            if os.path.exists(raw_img_path):
                shutil.copy2(raw_img_path, target_img_path)
            else:
                print(f"Warning: Original image not found for {filename}")

print("Done grouping raw images!")
