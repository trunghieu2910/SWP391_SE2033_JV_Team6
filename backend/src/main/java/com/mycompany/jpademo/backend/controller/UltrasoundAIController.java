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

            // Kiểm tra dung lượng file tối thiểu (10KB)
            if (file.getSize() < 10240) {
                return ResponseEntity.badRequest().body("Vui lòng chọn ảnh chất lượng hơn.");
            }

            // Kiểm tra định dạng file
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
            }
            if (!ext.equals(".jpg") && !ext.equals(".jpeg") && !ext.equals(".png") && !ext.equals(".dcm") && !ext.equals(".dicom")) {
                return ResponseEntity.badRequest().body("Định dạng file không hỗ trợ. Hệ thống chỉ chấp nhận JPG, PNG hoặc DICOM.");
            }
            
            // 2. Tạo thư mục lưu file
            Path uploadDir = Paths.get("uploads");
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            String originalFileName = "orig_" + UUID.randomUUID().toString() + ext;
            Path originalPath = uploadDir.resolve(originalFileName);
            Files.copy(file.getInputStream(), originalPath, StandardCopyOption.REPLACE_EXISTING);
            
            String originalUrlPath = null;
            byte[] predictImageBytes = null;

            // 3. Nếu là file DICOM, gọi Python chuyển đổi sang JPEG trước khi lưu và phân tích
            if (ext.equals(".dcm") || ext.equals(".dicom")) {
                String convertUrl = "http://localhost:5000/convert-dicom";
                HttpHeaders convertHeaders = new HttpHeaders();
                convertHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
                
                final String finalExt = ext;
                MultiValueMap<String, Object> convertBody = new LinkedMultiValueMap<>();
                convertBody.add("file", new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return "image" + finalExt;
                    }
                });
                
                HttpEntity<MultiValueMap<String, Object>> convertRequestEntity = new HttpEntity<>(convertBody, convertHeaders);
                ResponseEntity<byte[]> convertResponse;
                try {
                    convertResponse = restTemplate.postForEntity(convertUrl, convertRequestEntity, byte[].class);
                } catch (org.springframework.web.client.HttpStatusCodeException e) {
                    String errorBody = e.getResponseBodyAsString();
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(errorBody);
                        if (node.has("error")) {
                            return ResponseEntity.badRequest().body(node.get("error").asText());
                        }
                    } catch (Exception jsonEx) {}
                    return ResponseEntity.badRequest().body("Lỗi kiểm tra chất lượng DICOM: " + errorBody);
                } catch (Exception e) {
                    return ResponseEntity.status(500).body("Không thể kết nối Python AI Service ở cổng 5000");
                }

                if (convertResponse.getStatusCode() == HttpStatus.OK && convertResponse.getBody() != null) {
                    // Lưu ảnh JPEG được trích xuất từ DICOM để trình duyệt có thể hiển thị
                    String origJpgName = "orig_" + UUID.randomUUID().toString() + ".jpg";
                    Path origJpgPath = uploadDir.resolve(origJpgName);
                    Files.write(origJpgPath, convertResponse.getBody());
                    originalUrlPath = "/uploads/" + origJpgName;
                    predictImageBytes = convertResponse.getBody();
                } else {
                    return ResponseEntity.status(500).body("Lỗi chuyển đổi file DICOM sang ảnh JPEG.");
                }
            } else {
                originalUrlPath = "/uploads/" + originalFileName;
                predictImageBytes = file.getBytes();
            }
            
            // 4. Gửi sang Python AI
            String aiServiceUrl = "http://localhost:5000/predict";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            
            final byte[] bytesToSend = predictImageBytes;
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(bytesToSend) {
                @Override
                public String getFilename() {
                    return "image.jpg";
                }
            });
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<byte[]> aiResponse;
            try {
                aiResponse = restTemplate.postForEntity(aiServiceUrl, requestEntity, byte[].class);
            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                String errorBody = e.getResponseBodyAsString();
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(errorBody);
                    if (node.has("error")) {
                        return ResponseEntity.badRequest().body(node.get("error").asText());
                    }
                } catch (Exception jsonEx) {}
                return ResponseEntity.badRequest().body(errorBody);
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

            // 5. Lưu vào Database
            MedicalImage mi = new MedicalImage();
            mi.setDiagnosisSession(sessionOpt.get());
            mi.setImageType("Siêu âm bụng");
            mi.setStatus(com.mycompany.jpademo.backend.enums.MedicalImageStatus.COMPLETED);
            mi = medicalImageRepository.save(mi);
            
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
                    detail.getConfidenceScore(),
                    detail.getTechnicalConclusion(),
                    detail.getImgResultConclusion()
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
            detail.getConfidenceScore(),
            detail.getTechnicalConclusion(),
            detail.getImgResultConclusion()
        ));
    }

    @PostMapping("/ultrasound/process-ai/{id}")
    public ResponseEntity<?> processAI(@PathVariable Integer id) {
        Optional<MedicalImageDetails> opt = repository.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        MedicalImageDetails detail = opt.get();

        try {
            // Tải ảnh gốc từ URL hoặc file local
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

            String ext = "";
            if (originalUrl.contains(".")) {
                ext = originalUrl.substring(originalUrl.lastIndexOf(".")).toLowerCase();
            }

            byte[] predictImageBytes = imageBytes;
            
            // Nếu file gốc là DICOM, cần chuyển đổi trước
            if (ext.equals(".dcm") || ext.equals(".dicom")) {
                String convertUrl = "http://localhost:5000/convert-dicom";
                HttpHeaders convertHeaders = new HttpHeaders();
                convertHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);
                
                final byte[] dcmBytes = imageBytes;
                final String finalExt = ext;
                MultiValueMap<String, Object> convertBody = new LinkedMultiValueMap<>();
                convertBody.add("file", new ByteArrayResource(dcmBytes) {
                    @Override
                    public String getFilename() {
                        return "image" + finalExt;
                    }
                });
                
                HttpEntity<MultiValueMap<String, Object>> convertRequestEntity = new HttpEntity<>(convertBody, convertHeaders);
                ResponseEntity<byte[]> convertResponse;
                try {
                    convertResponse = restTemplate.postForEntity(convertUrl, convertRequestEntity, byte[].class);
                } catch (org.springframework.web.client.HttpStatusCodeException e) {
                    String errorBody = e.getResponseBodyAsString();
                    try {
                        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(errorBody);
                        if (node.has("error")) {
                            return ResponseEntity.badRequest().body(node.get("error").asText());
                        }
                    } catch (Exception jsonEx) {}
                    return ResponseEntity.badRequest().body("Lỗi kiểm tra chất lượng DICOM: " + errorBody);
                } catch (Exception e) {
                    return ResponseEntity.status(500).body("Không thể kết nối Python AI Service ở cổng 5000");
                }

                if (convertResponse.getStatusCode() == HttpStatus.OK && convertResponse.getBody() != null) {
                    predictImageBytes = convertResponse.getBody();
                } else {
                    return ResponseEntity.status(500).body("Lỗi chuyển đổi file DICOM sang ảnh JPEG.");
                }
            }

            // Gửi ảnh sang Python AI Service
            String aiServiceUrl = "http://localhost:5000/predict";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            final byte[] bytesToSend = predictImageBytes;
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(bytesToSend) {
                @Override
                public String getFilename() {
                    return "image.jpg";
                }
            });

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<byte[]> response;
            try {
                response = restTemplate.postForEntity(aiServiceUrl, requestEntity, byte[].class);
            } catch (org.springframework.web.client.HttpStatusCodeException e) {
                String errorBody = e.getResponseBodyAsString();
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    com.fasterxml.jackson.databind.JsonNode node = mapper.readTree(errorBody);
                    if (node.has("error")) {
                        return ResponseEntity.badRequest().body(node.get("error").asText());
                    }
                } catch (Exception jsonEx) {}
                return ResponseEntity.badRequest().body(errorBody);
            } catch (Exception e) {
                return ResponseEntity.status(500).body("Lỗi kết nối Python AI Service: " + e.getMessage());
            }

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
                    detail.getConfidenceScore(),
                    detail.getTechnicalConclusion(),
                    detail.getImgResultConclusion()
                ));
            } else {
                return ResponseEntity.status(500).body("AI Service trả về lỗi");
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PostMapping("/ultrasound/save-conclusion/{imageId}")
    public ResponseEntity<?> saveConclusion(@PathVariable Integer imageId, @RequestBody TechnicalConclusionRequest request) {
        Optional<MedicalImageDetails> opt = repository.findById(imageId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        MedicalImageDetails detail = opt.get();
        
        try {
            if (request.conclusion != null) {
                detail.setTechnicalConclusion(request.conclusion);
            }
            
            if (request.manualImageBase64 != null && !request.manualImageBase64.isEmpty()) {
                // Decode base64
                String base64Image = request.manualImageBase64.split(",")[1];
                byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Image);
                
                String newFileName = "manual_" + UUID.randomUUID().toString() + ".jpg";
                Path aiPath = Paths.get("uploads", newFileName);
                if (!Files.exists(aiPath.getParent())) {
                    Files.createDirectories(aiPath.getParent());
                }
                Files.write(aiPath, imageBytes);
                
                detail.setImgResultConclusion("/uploads/" + newFileName);
            }
            
            repository.save(detail);
            return ResponseEntity.ok("Đã lưu kết luận thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Lỗi khi lưu kết luận: " + e.getMessage());
        }
    }

    public static class TechnicalConclusionRequest {
        public String conclusion;
        public String manualImageBase64;
    }

    public static class ImageDetailDto {
        public Integer imageId;
        public String imageType;
        public String imageUrl;
        public String aiImageUrl;
        public Double confidenceScore;
        public String technicalConclusion;
        public String manualAiImageUrl;

        public ImageDetailDto(Integer imageId, String imageType, String imageUrl, String aiImageUrl, Double confidenceScore, String technicalConclusion, String manualAiImageUrl) {
            this.imageId = imageId;
            this.imageType = imageType;
            this.imageUrl = imageUrl;
            this.aiImageUrl = aiImageUrl;
            this.confidenceScore = confidenceScore;
            this.technicalConclusion = technicalConclusion;
            this.manualAiImageUrl = manualAiImageUrl;
        }
    }
}
