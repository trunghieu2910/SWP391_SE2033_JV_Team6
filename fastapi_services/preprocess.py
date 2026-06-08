"""
Module tiền xử lý ảnh siêu âm dùng chung cho tất cả FastAPI services.

Pipeline:
    1. Crop vùng siêu âm (bỏ viền đen, thông tin bệnh nhân)
    2. Xóa caliper/annotation (dùng morphology + inpainting)
    3. Chuyển sang grayscale 3 kênh (khớp với dữ liệu huấn luyện)
"""

import cv2
import numpy as np
import io
from typing import Tuple


def preprocess_ultrasound_from_bytes(image_bytes: bytes) -> np.ndarray:
    """
    Tiền xử lý ảnh siêu âm từ raw bytes (nhận từ HTTP upload).

    Args:
        image_bytes: Dữ liệu ảnh dạng bytes (từ UploadFile.read())

    Returns:
        np.ndarray: Ảnh đã tiền xử lý (BGR, 3 kênh grayscale)

    Raises:
        ValueError: Nếu không decode được ảnh
    """
    # Decode bytes -> numpy array
    nparr = np.frombuffer(image_bytes, np.uint8)
    img = cv2.imdecode(nparr, cv2.IMREAD_COLOR)

    if img is None:
        raise ValueError("Không thể decode ảnh từ dữ liệu upload. Vui lòng kiểm tra file ảnh.")

    return _preprocess_pipeline(img)


def preprocess_ultrasound_from_path(img_path: str) -> np.ndarray:
    """
    Tiền xử lý ảnh siêu âm từ đường dẫn file.

    Args:
        img_path: Đường dẫn tới file ảnh

    Returns:
        np.ndarray: Ảnh đã tiền xử lý (BGR, 3 kênh grayscale)

    Raises:
        FileNotFoundError: Nếu không đọc được ảnh
    """
    img = cv2.imread(img_path)
    if img is None:
        raise FileNotFoundError(f"Không đọc được ảnh: {img_path}")

    return _preprocess_pipeline(img)


def _preprocess_pipeline(img: np.ndarray) -> np.ndarray:
    """
    Pipeline tiền xử lý chính (giống hệt deploy scripts hiện tại).

    Steps:
        1. Crop vùng siêu âm: top 15%, bottom 5%, left 15%, right 15%
        2. Morphology Top-hat + Black-hat để phát hiện caliper
        3. Inpainting để xóa caliper
        4. Thêm noise nhẹ vào vùng đã inpaint cho tự nhiên
        5. Chuyển sang grayscale 3 kênh

    Args:
        img: Ảnh gốc BGR

    Returns:
        np.ndarray: Ảnh đã tiền xử lý
    """
    orig_h, orig_w = img.shape[:2]

    # Bước 1: Crop vùng siêu âm
    crop_y1 = int(orig_h * 0.15)
    crop_y2 = int(orig_h * 0.95)
    crop_x1 = int(orig_w * 0.15)
    crop_x2 = int(orig_w * 0.85)
    cropped = img[crop_y1:crop_y2, crop_x1:crop_x2]

    # Bước 2: Phát hiện caliper bằng morphology
    gray = cv2.cvtColor(cropped, cv2.COLOR_BGR2GRAY)
    kernel = cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (5, 5))
    tophat = cv2.morphologyEx(gray, cv2.MORPH_TOPHAT, kernel)
    blackhat = cv2.morphologyEx(gray, cv2.MORPH_BLACKHAT, kernel)
    combined = cv2.add(tophat, blackhat)
    _, mask = cv2.threshold(combined, 35, 255, cv2.THRESH_BINARY)
    mask_dilated = cv2.dilate(mask, np.ones((3, 3), np.uint8), iterations=1)

    # Bước 3: Inpainting xóa caliper
    inpainted = cv2.inpaint(cropped, mask_dilated, 3, cv2.INPAINT_TELEA)

    # Bước 4: Thêm noise cho tự nhiên
    noise = np.zeros(inpainted.shape, np.int16)
    cv2.randn(noise, mean=0, stddev=8)
    noisy = cv2.add(inpainted, noise, dtype=cv2.CV_8UC3)
    mask_3ch = cv2.cvtColor(mask_dilated, cv2.COLOR_GRAY2BGR) / 255.0
    healed = (noisy * mask_3ch + inpainted * (1 - mask_3ch)).astype(np.uint8)

    # Bước 5: Chuyển sang grayscale 3 kênh
    gray_final = cv2.cvtColor(healed, cv2.COLOR_BGR2GRAY)
    result = cv2.cvtColor(gray_final, cv2.COLOR_GRAY2BGR)

    return result
