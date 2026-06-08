# 🔗 Hướng dẫn tích hợp FastAPI AI Service vào Java Spring Boot

> API phát hiện u xơ tử cung (Myoma) — YOLO26s-seg

---

## ✅ Tổng quan kiến trúc

```
[User/Frontend]
      │  gửi ảnh siêu âm
      ▼
[Java Spring Boot] ──── HTTP POST ────► [Python FastAPI AI]
      │                                       │
      │◄──────── JSON Response ───────────────┘
      │  (detections, bbox, mask, base64 image)
      ▼
[Frontend hiển thị kết quả]
```

**FastAPI chạy ở:** `http://localhost:8004` (ONNX Universal - CPU, chạy được mọi máy)

---

## 📦 Bước 1 — Thêm dependency vào `pom.xml`

```xml
<!-- Dùng RestTemplate hoặc WebClient, chọn 1 trong 2 -->

<!-- Cách 1: RestTemplate (đơn giản hơn, đồng bộ) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- Cách 2: WebClient (reactive, không blocking) -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- Jackson để parse JSON (thường đã có sẵn) -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

---

## 📐 Bước 2 — Tạo các DTO (Data Transfer Objects)

### `DetectionResult.java`
```java
package com.yourproject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DetectionResult {

    @JsonProperty("class_id")
    private int classId;

    @JsonProperty("class_name")
    private String className;

    @JsonProperty("confidence")
    private double confidence;

    @JsonProperty("bbox")
    private List<Double> bbox;          // [x1, y1, x2, y2]

    @JsonProperty("mask_polygon")
    private List<List<Double>> maskPolygon;  // [[x,y], [x,y], ...]  — có thể null

    // Getters & Setters
    public int getClassId() { return classId; }
    public void setClassId(int classId) { this.classId = classId; }

    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }

    public List<Double> getBbox() { return bbox; }
    public void setBbox(List<Double> bbox) { this.bbox = bbox; }

    public List<List<Double>> getMaskPolygon() { return maskPolygon; }
    public void setMaskPolygon(List<List<Double>> maskPolygon) { this.maskPolygon = maskPolygon; }
}
```

### `PredictionResponse.java`
```java
package com.yourproject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class PredictionResponse {

    @JsonProperty("success")
    private boolean success;

    @JsonProperty("backend")
    private String backend;

    @JsonProperty("inference_time_ms")
    private double inferenceTimeMs;

    @JsonProperty("image_width")
    private int imageWidth;

    @JsonProperty("image_height")
    private int imageHeight;

    @JsonProperty("num_detections")
    private int numDetections;

    @JsonProperty("detections")
    private List<DetectionResult> detections;

    @JsonProperty("annotated_image_base64")
    private String annotatedImageBase64;  // Ảnh đã vẽ bbox, encode Base64

    // Getters & Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getBackend() { return backend; }
    public void setBackend(String backend) { this.backend = backend; }

    public double getInferenceTimeMs() { return inferenceTimeMs; }
    public void setInferenceTimeMs(double inferenceTimeMs) { this.inferenceTimeMs = inferenceTimeMs; }

    public int getImageWidth() { return imageWidth; }
    public void setImageWidth(int imageWidth) { this.imageWidth = imageWidth; }

    public int getImageHeight() { return imageHeight; }
    public void setImageHeight(int imageHeight) { this.imageHeight = imageHeight; }

    public int getNumDetections() { return numDetections; }
    public void setNumDetections(int numDetections) { this.numDetections = numDetections; }

    public List<DetectionResult> getDetections() { return detections; }
    public void setDetections(List<DetectionResult> detections) { this.detections = detections; }

    public String getAnnotatedImageBase64() { return annotatedImageBase64; }
    public void setAnnotatedImageBase64(String annotatedImageBase64) { this.annotatedImageBase64 = annotatedImageBase64; }
}
```

### `ImagePathRequest.java` (dùng khi gọi `/predict/path`)
```java
package com.yourproject.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ImagePathRequest {

    @JsonProperty("image_path")
    private String imagePath;

    @JsonProperty("confidence")
    private double confidence = 0.25;

    // Constructor
    public ImagePathRequest(String imagePath) {
        this.imagePath = imagePath;
    }

    public ImagePathRequest(String imagePath, double confidence) {
        this.imagePath = imagePath;
        this.confidence = confidence;
    }

    // Getters & Setters
    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public double getConfidence() { return confidence; }
    public void setConfidence(double confidence) { this.confidence = confidence; }
}
```

---

## ⚙️ Bước 3 — Cấu hình Bean trong `AppConfig.java`

```java
package com.yourproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5_000);   // 5 giây kết nối
        factory.setReadTimeout(30_000);     // 30 giây chờ kết quả AI (AI xử lý lâu!)
        return new RestTemplate(factory);
    }
}
```

> [!IMPORTANT]
> Phải set `ReadTimeout` đủ lớn (≥ 15-30 giây). Model AI inference có thể mất vài giây.

---

## 🔧 Bước 4 — Tạo `AIService.java`

```java
package com.yourproject.service;

import com.yourproject.dto.DetectionResult;
import com.yourproject.dto.ImagePathRequest;
import com.yourproject.dto.PredictionResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
public class AIService {

    @Autowired
    private RestTemplate restTemplate;

    // Đặt trong application.properties: ai.service.url=http://localhost:8004
    @Value("${ai.service.url:http://localhost:8004}")
    private String aiServiceUrl;

    // ─────────────────────────────────────────────
    // CÁCH 1: Upload file ảnh trực tiếp (POST /predict)
    // Dùng khi: frontend upload ảnh lên Java, Java chuyển tiếp sang AI
    // ─────────────────────────────────────────────
    public PredictionResponse predictByUpload(MultipartFile imageFile) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Bọc file vào ByteArrayResource để gửi qua HTTP
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new ByteArrayResource(imageFile.getBytes()) {
            @Override
            public String getFilename() {
                return imageFile.getOriginalFilename();
            }
        });
        body.add("confidence", "0.25");  // Tuỳ chỉnh ngưỡng confidence

        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);

        ResponseEntity<PredictionResponse> response = restTemplate.postForEntity(
            aiServiceUrl + "/predict",
            request,
            PredictionResponse.class
        );

        return response.getBody();
    }

    // ─────────────────────────────────────────────
    // CÁCH 2: Gửi đường dẫn ảnh (POST /predict/path)
    // Dùng khi: ảnh đã lưu trên cùng máy với FastAPI server
    // ─────────────────────────────────────────────
    public PredictionResponse predictByPath(String imagePath) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ImagePathRequest requestBody = new ImagePathRequest(imagePath, 0.25);
        HttpEntity<ImagePathRequest> request = new HttpEntity<>(requestBody, headers);

        ResponseEntity<PredictionResponse> response = restTemplate.postForEntity(
            aiServiceUrl + "/predict/path",
            request,
            PredictionResponse.class
        );

        return response.getBody();
    }

    // ─────────────────────────────────────────────
    // CÁCH 3: Kiểm tra AI service còn sống không
    // Dùng trong health check hoặc trước khi gọi predict
    // ─────────────────────────────────────────────
    public boolean isAIServiceHealthy() {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                aiServiceUrl + "/health",
                String.class
            );
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }

    // ─────────────────────────────────────────────
    // HELPER: Lấy danh sách detections từ response
    // ─────────────────────────────────────────────
    public List<DetectionResult> getDetections(PredictionResponse response) {
        if (response != null && response.isSuccess()) {
            return response.getDetections();
        }
        return List.of();
    }
}
```

---

## 🌐 Bước 5 — Tạo `AIController.java`

```java
package com.yourproject.controller;

import com.yourproject.dto.PredictionResponse;
import com.yourproject.service.AIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AIController {

    @Autowired
    private AIService aiService;

    /**
     * Frontend gửi ảnh siêu âm → Java → FastAPI → trả kết quả
     * POST /api/ai/predict
     * Content-Type: multipart/form-data
     * Body: file = <image file>
     */
    @PostMapping("/predict")
    public ResponseEntity<?> predict(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File ảnh không được rỗng"));
        }

        try {
            PredictionResponse result = aiService.predictByUpload(file);

            if (result == null || !result.isSuccess()) {
                return ResponseEntity.internalServerError()
                    .body(Map.of("error", "AI service không trả về kết quả hợp lệ"));
            }

            return ResponseEntity.ok(result);

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Lỗi đọc file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Lỗi kết nối AI service: " + e.getMessage()));
        }
    }

    /**
     * Gửi đường dẫn ảnh trên server → FastAPI đọc file trực tiếp
     * POST /api/ai/predict/path
     * Body: { "imagePath": "D:/uploads/image.jpg" }
     */
    @PostMapping("/predict/path")
    public ResponseEntity<?> predictByPath(@RequestBody Map<String, String> body) {
        String imagePath = body.get("imagePath");
        if (imagePath == null || imagePath.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "imagePath không được rỗng"));
        }

        try {
            PredictionResponse result = aiService.predictByPath(imagePath);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Lỗi: " + e.getMessage()));
        }
    }

    /**
     * Kiểm tra AI service còn sống không
     * GET /api/ai/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        boolean healthy = aiService.isAIServiceHealthy();
        return ResponseEntity.ok(Map.of(
            "ai_service_status", healthy ? "UP" : "DOWN",
            "message", healthy ? "AI service đang hoạt động" : "AI service không phản hồi"
        ));
    }
}
```

---

## 📝 Bước 6 — Cấu hình `application.properties`

```properties
# URL của FastAPI AI Service
ai.service.url=http://localhost:8004

# Upload file size limit (ảnh siêu âm có thể lớn)
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB

# Timeout (tuỳ Spring Boot version)
spring.mvc.async.request-timeout=60000
```

---

## 🖼️ Bước 7 — Hiển thị ảnh kết quả (annotated_image_base64)

FastAPI trả về ảnh đã vẽ bounding box dưới dạng **Base64**. Frontend dùng như sau:

### Frontend HTML/JS:
```html
<img id="resultImage" src="" alt="Kết quả AI" />

<script>
fetch('/api/ai/predict', {
    method: 'POST',
    body: formData  // formData chứa file ảnh
})
.then(res => res.json())
.then(data => {
    if (data.annotated_image_base64) {
        document.getElementById('resultImage').src =
            'data:image/jpeg;base64,' + data.annotated_image_base64;
    }
    console.log('Số u phát hiện:', data.num_detections);
    console.log('Thời gian AI:', data.inference_time_ms, 'ms');
});
</script>
```

---

## 🗺️ Sơ đồ luồng dữ liệu đầy đủ

```
Frontend (React/Thymeleaf/...)
    │
    │  POST /api/ai/predict
    │  multipart/form-data { file: image.jpg }
    ▼
AIController.java  (Spring Boot)
    │
    │  gọi aiService.predictByUpload(file)
    ▼
AIService.java
    │
    │  POST http://localhost:8004/predict
    │  multipart/form-data { file: ByteArrayResource }
    ▼
FastAPI Python Server (port 8004)
    │
    │  YOLO model inference
    │  preprocess → model → postprocess
    ▼
    Response JSON:
    {
      "success": true,
      "num_detections": 2,
      "detections": [{ "class_name": "Myoma", "confidence": 0.92, "bbox": [...] }],
      "annotated_image_base64": "..."
    }
    │
    ▼
AIService.java → AIController.java → Frontend
```

---

## ⚠️ Lưu ý quan trọng

> [!WARNING]
> **2 server phải cùng chạy một lúc:**
> - Java Spring Boot (ví dụ port 8080)
> - Python FastAPI (port 8004)
>
> Nếu FastAPI chưa khởi động, Java sẽ nhận lỗi connection refused.

> [!IMPORTANT]
> **Khi deploy production (2 máy khác nhau):**
> Đổi `ai.service.url` trong `application.properties`:
> ```properties
> ai.service.url=http://<IP_máy_AI>:8004
> ```

> [!TIP]
> **Chọn endpoint nào?**
> | Tình huống | Dùng endpoint |
> |---|---|
> | Frontend upload ảnh, Java chuyển tiếp | `POST /predict` (multipart) |
> | Ảnh đã lưu sẵn trên cùng máy với FastAPI | `POST /predict/path` (JSON) |
> | Kiểm tra AI còn sống trước khi gọi | `GET /health` |

---

## 🧪 Test nhanh bằng curl (không cần viết code Java)

```bash
# Test health
curl http://localhost:8004/health

# Test upload ảnh
curl -X POST http://localhost:8004/predict \
  -F "file=@D:/test/ultrasound.jpg" \
  -F "confidence=0.25"

# Test path
curl -X POST http://localhost:8004/predict/path \
  -H "Content-Type: application/json" \
  -d "{\"image_path\": \"D:/test/ultrasound.jpg\", \"confidence\": 0.25}"
```
