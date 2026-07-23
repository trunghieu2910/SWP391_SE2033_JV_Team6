package com.mycompany.jpademo.backend.util;

import com.mycompany.jpademo.backend.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Validate và sinh tên file an toàn cho các upload nhạy cảm (chứng chỉ, ảnh y tế...).
 */
public final class SecureFileUploadUtil {

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // extension cho phép -> magic number (byte đầu) tương ứng để đối chiếu nội dung thật
    private static final Map<String, byte[]> ALLOWED_SIGNATURES = Map.of(
            ".pdf", new byte[]{0x25, 0x50, 0x44, 0x46},
            ".png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
            ".jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            ".jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
    );

    private static final Set<String> ALLOWED_EXTENSIONS = ALLOWED_SIGNATURES.keySet();

    private SecureFileUploadUtil() {}

    /**
     * Validate toàn diện 1 file upload: kích thước, extension khai báo, và nội dung thật.
     * Ném BadRequestException nếu không hợp lệ ở bất kỳ bước nào.
     * Trả về tên file MỚI (UUID) an toàn để lưu, không liên quan gì tới tên gốc.
     */
    public static String validateAndGenerateSafeFileName(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File không được để trống.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File vượt quá dung lượng cho phép (tối đa 5MB).");
        }

        String declaredExt = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(declaredExt)) {
            throw new BadRequestException(
                    "Định dạng file không được hỗ trợ. Chỉ chấp nhận: " + ALLOWED_EXTENSIONS);
        }

        if (!matchesRealContent(file, declaredExt)) {
            throw new BadRequestException(
                    "Nội dung file không khớp với định dạng khai báo (" + declaredExt + ").");
        }

        return UUID.randomUUID() + declaredExt;
    }

    /**
     * Validate toàn diện 1 file upload và trả về tên file theo định dạng custom.
     */
    public static String validateAndGenerateCustomFileName(MultipartFile file, String customName) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File không được để trống.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File vượt quá dung lượng cho phép (tối đa 5MB).");
        }

        String declaredExt = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(declaredExt)) {
            throw new BadRequestException(
                    "Định dạng file không được hỗ trợ. Chỉ chấp nhận: " + ALLOWED_EXTENSIONS);
        }

        if (!matchesRealContent(file, declaredExt)) {
            throw new BadRequestException(
                    "Nội dung file không khớp với định dạng khai báo (" + declaredExt + ").");
        }

        // Đảm bảo tên custom an toàn cho hệ thống file
        String safeCustomName = customName.replaceAll("[^a-zA-Z0-9_.-]", "_");
        return safeCustomName + declaredExt;
    }


    // Đảm bảo path ghi file thực sự nằm trong thư mục cho phép.
    public static Path resolveSafely(Path baseDir, String safeFileName) {
        Path target = baseDir.resolve(safeFileName).normalize();
        Path normalizedBase = baseDir.normalize();
        if (!target.startsWith(normalizedBase)) {
            throw new BadRequestException("Đường dẫn file không hợp lệ.");
        }
        return target;
    }

    private static String extractExtension(String originalFilename) {
        if (originalFilename == null) return "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == originalFilename.length() - 1) return "";
        return originalFilename.substring(dotIndex).toLowerCase();
    }

    //Đọc vài byte đầu để xác nhận nội dung thật, không tin đuôi file hay Content-Type.
    private static boolean matchesRealContent(MultipartFile file, String declaredExt) {
        byte[] expectedSignature = ALLOWED_SIGNATURES.get(declaredExt);
        if (expectedSignature == null) return false;

        byte[] header = new byte[expectedSignature.length];
        try (InputStream is = file.getInputStream()) {
            int read = is.read(header);
            if (read < expectedSignature.length) return false;
        } catch (IOException e) {
            return false;
        }

        for (int i = 0; i < expectedSignature.length; i++) {
            if (header[i] != expectedSignature[i]) return false;
        }
        return true;
    }
}