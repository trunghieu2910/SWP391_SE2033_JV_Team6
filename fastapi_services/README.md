# 🏥 FastAPI AI Services - YOLO26s-seg
## Phát hiện u xơ tử cung (Myoma) từ ảnh siêu âm

---

## 📁 Cấu trúc thư mục

```
fastapi_services/
├── preprocess.py              # Module tiền xử lý ảnh dùng chung
├── requirements.txt           # Danh sách thư viện cần cài
├── fastapi_pytorch.py         # Service cho model .pt       (Port 8000)  MÁY BẤT KÌ 
├── fastapi_tensorrt.py        # Service cho model .engine   (Port 8001)  VỚI MÁY CÓ CARD RỜI 
├── fastapi_openvino.py        # Service cho model OpenVINO  (Port 8002)  MÁY CÓ CHIP INTEL KO CÓ CARD RỜI   ==> ĐA NỀN TẢNG WIN VÀ LINUX 
├── fastapi_onnx_directml.py   # Service cho model .onnx DML (Port 8003)  THƯ VIỆN CŨ  : ĐẢM BẢO CHYAJ ĐC NHM HIỆU SUẤT KO CAO    
├── fastapi_onnx_universal.py  # Service cho model .onnx CPU (Port 8004)  CHẠY TRÊN CPU 
├── fastapi_onnx_winml.py      # Service cho model .onnx Win (Port 8005)  THƯ VIỆN MỚI HƠN : ĐẢM BẢO CHYAJ ĐC NHM HIỆU SUẤT KO CAO  
├── fastapi_ncnn.py            # Service cho model NCNN      (Port 8006)  CHYAJ TRÊN THƯ VIỆN  ĐỒ HỌA VULKAN ==> CÓ THỂ CHẬY TRÊN LINUX Và win ==> CHIP NÀO CŨNG ĐC 
└── README.md                  # File này
```

---

## 🚀 Cách chạy

### Bước 1: Cài đặt thư viện
```bash
cd YOLO26s-seg/fastapi_services
pip install -r requirements.txt
```

### Bước 2: Sửa đường dẫn model
Mở file FastAPI tương ứng, sửa biến `MODEL_PATH` cho đúng đường dẫn model trên máy.

### Bước 3: Chạy server (chọn 1 trong 6)
```bash
# PyTorch (.pt) - Port 8000
uvicorn fastapi_pytorch:app --host 0.0.0.0 --port 8000

# TensorRT (.engine) - Port 8001
uvicorn fastapi_tensorrt:app --host 0.0.0.0 --port 8001

# OpenVINO (folder) - Port 8002
uvicorn fastapi_openvino:app --host 0.0.0.0 --port 8002

# ONNX DirectML (.onnx) - Port 8003
uvicorn fastapi_onnx_directml:app --host 0.0.0.0 --port 8003

# ONNX Universal (.onnx) - Port 8004
uvicorn fastapi_onnx_universal:app --host 0.0.0.0 --port 8004

# ONNX WinML (.onnx) - Port 8005
uvicorn fastapi_onnx_winml:app --host 0.0.0.0 --port 8005

# NCNN Vulkan (folder) - Port 8006
uvicorn fastapi_ncnn:app --host 0.0.0.0 --port 8006
```

### Bước 4: Test API
Truy cập Swagger UI tự động tại: `http://localhost:<PORT>/docs`

---

## 📡 API Endpoints

Tất cả 6 service đều có **cùng API contract** (cùng input/output):

### `GET /health`
Kiểm tra service còn sống không. Spring Boot nên gọi endpoint này định kỳ.

**Response:**
```json
{
  "status": "healthy",
  "backend": "PyTorch (.pt)",
  "model_loaded": true
}
```

### `POST /predict` (Upload file ảnh)
Spring Boot gửi file ảnh trực tiếp qua multipart/form-data.

**Request:** `multipart/form-data`
- `file`: File ảnh siêu âm (.jpg, .png)
- `confidence`: Ngưỡng confidence (mặc định: 0.25)

### `POST /predict/path` (Gửi đường dẫn ảnh)
Spring Boot gửi đường dẫn ảnh đã lưu trên server.

**Request:** `application/json`
```json
{
  "image_path": "D:/uploads/ultrasound_001.jpg",
  "confidence": 0.25
}
```

### Response chung cho cả 2 endpoint predict:
```json
{
  "success": true,
  "backend": "PyTorch (.pt)",
  "inference_time_ms": 45.23,
  "image_width": 448,
  "image_height": 512,
  "num_detections": 2,
  "detections": [
    {
      "class_id": 0,
      "class_name": "Myoma",
      "confidence": 0.9234,
      "bbox": [120.5, 200.3, 350.8, 410.6],
      "mask_polygon": [[120.5, 200.3], [125.0, 198.7], ...]
    }
  ],
  "annotated_image_base64": "/9j/4AAQ..."
}
```

---

## 🔗 Cách Spring Boot gọi FastAPI

### Dùng RestTemplate (Java):
```java
@Service
public class AIService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String AI_SERVICE_URL = "http://localhost:8000";

    public PredictionResponse predict(MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        });

        HttpEntity<MultiValueMap<String, Object>> request =
            new HttpEntity<>(body, headers);

        ResponseEntity<PredictionResponse> response = restTemplate.postForEntity(
            AI_SERVICE_URL + "/predict",
            request,
            PredictionResponse.class
        );

        return response.getBody();
    }
}
```

### Dùng WebClient (Spring WebFlux):
```java
@Service
public class AIService {

    private final WebClient webClient = WebClient.builder()
        .baseUrl("http://localhost:8000")
        .build();

    public Mono<PredictionResponse> predict(MultipartFile file) {
        return webClient.post()
            .uri("/predict")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData("file",
                new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                }))
            .retrieve()
            .bodyToMono(PredictionResponse.class);
    }
}
```

---

## ⚠️ Lưu ý quan trọng

1. **Chỉ cần chạy 1 service** tùy theo model format bạn có trên máy.
2. **Sửa `MODEL_PATH`** trong file FastAPI trước khi chạy.
3. **Spring Boot cần cấu hình timeout** đủ lớn (5-10 giây) khi gọi FastAPI.
4. **Endpoint `/predict`** dùng `def` (sync) cho tác vụ AI nặng - FastAPI tự động đưa vào ThreadPool.
5. Truy cập `http://localhost:<PORT>/docs` để test API trực tiếp trên trình duyệt (Swagger UI).
