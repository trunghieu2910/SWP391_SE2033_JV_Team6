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
    // extension cho phép -> magic number (byte đầu) tương ứng để đối chiếu nội dung thật
    private static final Map<String, byte[]> ALLOWED_SIGNATURES = Map.of(
            ".pdf", new byte[]{0x25, 0x50, 0x44, 0x46},
            ".png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47},
            ".jpg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF},
            ".jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}
    );

    private static final Set<String> ALLOWED_EXTENSIONS = ALLOWED_SIGNATURES.keySet();

    private SecureFileUploadUtil() {}

    public static String generateSafeCustomFileName(MultipartFile file, String customName) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("File không được để trống.");
        }

        String declaredExt = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(declaredExt)) {
            throw new BadRequestException(
                    "Định dạng file không được hỗ trợ. Chỉ chấp nhận: " + ALLOWED_EXTENSIONS);
        }

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

    public static boolean isValidSizeAndType(MultipartFile file, long maxSize) {
        if (file == null || file.isEmpty()) return false;
        if (file.getSize() > maxSize) return false;
        String declaredExt = extractExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(declaredExt)) return false;
        return matchesRealContent(file, declaredExt);
    }
}