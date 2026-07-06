package com.mycompany.jpademo.backend.controller;

import com.mycompany.jpademo.backend.entity.MedicalImageDetails;
import com.mycompany.jpademo.backend.repository.MedicalImageDetailsRepository;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;

import com.mycompany.jpademo.backend.entity.MedicalImage;
import com.mycompany.jpademo.backend.entity.DiagnosisSession;
import com.mycompany.jpademo.backend.repository.MedicalImageRepository;
import com.mycompany.jpademo.backend.repository.DiagnosisSessionRepository;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.StandardCopyOption;

@RestController
@RequestMapping("/api")
public class UltrasoundAIController {

    private final MedicalImageDetailsRepository repository;
    private final MedicalImageRepository medicalImageRepository;
    private final DiagnosisSessionRepository diagnosisSessionRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public UltrasoundAIController(MedicalImageDetailsRepository repository, 
                                  MedicalImageRepository medicalImageRepository, 
                                  DiagnosisSessionRepository diagnosisSessionRepository) {
        this.repository = repository;
        this.medicalImageRepository = medicalImageRepository;
        this.diagnosisSessionRepository = diagnosisSessionRepository;
    }

    @PostMapping("/ultrasound/upload")
    public ResponseEntity<?> uploadFromSimulator(@RequestParam("file") MultipartFile file, 
                                                 @RequestParam("sessionId") Integer sessionId) {
        try {
            // 1. Kiểm tra session
            Optional<DiagnosisSession> sessionOpt = diagnosisSessionRepository.findById(sessionId);
            if (sessionOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("Không tìm thấy phiên khám (Session ID: " + sessionId + ")");
            }
            
            // 2. Lưu file gốc
            String originalFileName = "orig_" + UUID.randomUUID().toString() + ".jpg";
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            Path originalPath = uploadDir.resolve(originalFileName);
            Files.copy(file.getInputStream(), originalPath, StandardCopyOption.REPLACE_EXISTING);
            String originalUrlPath = "/uploads/" + originalFileName;

            // 3. Gửi sang Python AI
            String aiServiceUrl = "http://localhost:5000/predict";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return "image.jpg";
                }
            });
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<byte[]> aiResponse;
            try {
                aiResponse = restTemplate.postForEntity(aiServiceUrl, requestEntity, byte[].class);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Không thể kết nối Python AI Service ở cổng 5000");
            }
            
            String aiUrlPath = null;
            Double confidenceScore = null;
            
            if (aiResponse.getStatusCode() == HttpStatus.OK && aiResponse.getBody() != null) {
                String aiFileName = "ai_" + UUID.randomUUID().toString() + ".jpg";
                Path aiPath = uploadDir.resolve(aiFileName);
                Files.write(aiPath, aiResponse.getBody());
                aiUrlPath = "/uploads/" + aiFileName;
                
                if (aiResponse.getHeaders().containsKey("X-AI-Confidence")) {
                    String confStr = aiResponse.getHeaders().getFirst("X-AI-Confidence");
                    if (confStr != null && !confStr.isEmpty()) {
                        confidenceScore = Double.parseDouble(confStr);
                    }
                }
            } else {
                return ResponseEntity.status(500).body("Lỗi xử lý AI");
            }

            // 4. Lưu vào Database
            // Tạo nhóm ảnh (MedicalImage)
            MedicalImage mi = new MedicalImage();
            mi.setDiagnosisSession(sessionOpt.get());
            mi.setImageType("Siêu âm bụng");
            mi.setStatus(com.mycompany.jpademo.backend.enums.MedicalImageStatus.COMPLETED);
            mi = medicalImageRepository.save(mi);
            
            // Tạo chi tiết ảnh (MedicalImageDetails)
            MedicalImageDetails detail = new MedicalImageDetails();
            detail.setMedicalImage(mi);
            detail.setImageUrl(originalUrlPath);
            detail.setAiImageUrl(aiUrlPath);
            detail.setConfidenceScore(confidenceScore);
            detail = repository.save(detail);
            
            return ResponseEntity.ok(new ImageDetailDto(
                    detail.getImageId(),
                    "Siêu âm bụng",
                    detail.getImageUrl(),
                    detail.getAiImageUrl(),
                    detail.getConfidenceScore()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi: " + e.getMessage());
        }
    }

    @GetMapping("/medical-images/{id}")
    public ResponseEntity<?> getImageDetails(@PathVariable Integer id) {
        Optional<MedicalImageDetails> opt = repository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        MedicalImageDetails detail = opt.get();
        
        return ResponseEntity.ok(new ImageDetailDto(
            detail.getImageId(),
            detail.getMedicalImage() != null ? detail.getMedicalImage().getImageType() : "Hình Ảnh Y Khoa",
            detail.getImageUrl(),
            detail.getAiImageUrl(),
            detail.getConfidenceScore()
        ));
    }

    @PostMapping("/ultrasound/process-ai/{id}")
    public ResponseEntity<?> processAI(@PathVariable Integer id) {
        Optional<MedicalImageDetails> opt = repository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        MedicalImageDetails detail = opt.get();

        try {
            // Tải ảnh gốc từ URL (vì db lưu dạng http) hoặc file local
            byte[] imageBytes;
            String originalUrl = detail.getImageUrl();
            if (originalUrl.startsWith("http")) {
                try (InputStream in = new URL(originalUrl).openStream()) {
                    imageBytes = in.readAllBytes();
                }
            } else {
                Path path = Paths.get("uploads", originalUrl.replace("/uploads/", ""));
                imageBytes = Files.readAllBytes(path);
            }

            // Gửi ảnh sang Python AI Service
            String aiServiceUrl = "http://localhost:5000/predict";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return "image.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<byte[]> response = restTemplate.postForEntity(aiServiceUrl, requestEntity, byte[].class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                // Lưu file ảnh AI
                String newFileName = "ai_" + UUID.randomUUID().toString() + ".jpg";
                Path aiPath = Paths.get("uploads", newFileName);
                if (!Files.exists(aiPath.getParent())) {
                    Files.createDirectories(aiPath.getParent());
                }
                Files.write(aiPath, response.getBody());
                
                String aiUrlPath = "/uploads/" + newFileName; 
                detail.setAiImageUrl(aiUrlPath);
                
                // Lấy độ tin cậy
                if (response.getHeaders().containsKey("X-AI-Confidence")) {
                    String confStr = response.getHeaders().getFirst("X-AI-Confidence");
                    if (confStr != null && !confStr.isEmpty()) {
                        detail.setConfidenceScore(Double.parseDouble(confStr));
                    }
                }
                
                repository.save(detail);

                return ResponseEntity.ok(new ImageDetailDto(
                    detail.getImageId(),
                    detail.getMedicalImage() != null ? detail.getMedicalImage().getImageType() : "Hình Ảnh Y Khoa",
                    detail.getImageUrl(),
                    detail.getAiImageUrl(),
                    detail.getConfidenceScore()
                ));
            } else {
                return ResponseEntity.status(500).body("AI Service trả về lỗi");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    public static class ImageDetailDto {
        public Integer imageId;
        public String imageType;
        public String imageUrl;
        public String aiImageUrl;
        public Double confidenceScore;

        public ImageDetailDto(Integer imageId, String imageType, String imageUrl, String aiImageUrl, Double confidenceScore) {
            this.imageId = imageId;
            this.imageType = imageType;
            this.imageUrl = imageUrl;
            this.aiImageUrl = aiImageUrl;
            this.confidenceScore = confidenceScore;
        }
    }
}
