package com.mycompany.jpademo.backend.service.impl;

import com.mycompany.jpademo.backend.dto.request.LisResultRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * In-memory catalog of canned lab result values, keyed by test type,
 * used to fake a real LIS response when a doctor clicks
 * "Lấy kết quả giả lập" instead of waiting on a real lab system.
 * The set of keys here is the single source of truth for which
 * test types are considered valid when creating a lab order.
 */
@Component
public class LisMockDataProvider {

    private final Map<String, List<LisResultRequest.TestResultItem>> mockData = new HashMap<>();

    /** Populates the mock dataset once at application startup. */
    @PostConstruct
    private void init() {
        mockData.put("Xét nghiệm tế bào học cổ tử cung", List.of(
                item("Chất lượng mẫu bệnh phẩm", "Đạt yêu cầu (Có tế bào vùng chuyển tiếp)", null),
                item("Tác nhân vi sinh", "Không phát hiện", null),
                item("Đánh giá tế bào tuyến", "Bình thường", null),
                item("Đánh giá tế bào vảy", "HSIL (Tổn thương mức độ cao)", null),
                item("Kết luận (The Bethesda System)", "Bất thường tế bào biểu mô vảy (HSIL)", null)
        ));

        mockData.put("Xét nghiệm DNA của virus HPV", List.of(
                item("Kiểm chứng nội bộ (IC)", "Hợp lệ", null),
                item("Phát hiện HPV nguy cơ cao", "Positive (Dương tính)", null),
                item("HPV Tuýp 16", "Positive", null),
                item("HPV Tuýp 18", "Negative", null),
                item("12 tuýp nguy cơ cao khác", "Negative", null),
                item("Ngưỡng chu kỳ (Ct Value) - Tuýp 16", "22.4", "Chu kỳ")
        ));

        mockData.put("Định tuýp HPV nguy cơ cao", List.of(
                item("Kiểm chứng nội bộ (IC)", "Đạt", null),
                item("Tuýp 16", "Positive", null),
                item("Tuýp 18", "Negative", null),
                item("Tuýp 31", "Positive", null),
                item("Tuýp 33", "Negative", null),
                item("Tuýp 45", "Negative", null),
                item("Tuýp 52", "Positive", null),
                item("Tuýp 58", "Negative", null),
                item("Các tuýp nguy cơ cao khác (35, 39, 51, 56, 59, 66, 68)", "Negative", null),
                item("Tuýp 6", "Negative", null),
                item("Tuýp 11", "Negative", null),
                item("Kết luận Định tuýp", "Nhiễm đa tuýp nguy cơ cao (16, 31, 52)", null)
        ));

        mockData.put("Sinh thiết", List.of(
                item("Mô tả đại thể", "Nhận 02 mảnh mô màu xám nhạt, kích thước lớn nhất 0.3x0.2cm.", null),
                item("Mô tả vi thể", "Biểu mô vảy quá sản, tế bào mất phân cực, nhân quái, nhân chia ở 2/3 bề dày lớp biểu mô. Màng đáy còn nguyên vẹn.", null),
                item("Kết luận Giải phẫu bệnh", "Tân sản nội biểu mô cổ tử cung độ 2 (CIN 2)", null)
        ));

        mockData.put("Dấu ấn ung thư SCC", List.of(
                item("Phương pháp phân tích", "Miễn dịch hóa phát quang (CMIA)", null),
                item("Mẫu bệnh phẩm", "Huyết thanh (Serum)", null),
                item("Nồng độ SCC Antigen", "12.40", "ng/mL"),
                item("Khoảng tham chiếu (Trị số BT)", "< 1.50", "ng/mL"),
                item("Đánh giá kết quả", "Tăng cao", null)
        ));

        mockData.put("Xét nghiệm máu cơ bản", List.of(
                item("Hồng cầu (RBC)", "2.8 ↓", "T/L"),
                item("Huyết sắc tố (HGB)", "85 ↓", "g/L"),
                item("Bạch cầu (WBC)", "1.5 ↓", "G/L"),
                item("Tiểu cầu (PLT)", "90 ↓", "G/L"),
                item("Ure máu", "5.0", "mmol/L"),
                item("Creatinin máu", "80", "µmol/L"),
                item("AST (Men gan)", "85 ↑", "U/L"),
                item("ALT (Men gan)", "90 ↑", "U/L"),
                item("Kết luận", "Thiếu máu, Giảm bạch cầu hạt, Tăng men gan", null)
        ));
    }

    /** Builds a single mock TestResultItem (name/value/unit) for readability above. */
    private LisResultRequest.TestResultItem item(String name, String value, String unit) {
        LisResultRequest.TestResultItem i = new LisResultRequest.TestResultItem();
        i.setTestName(name);
        i.setResultValue(value);
        i.setUnit(unit);
        return i;
    }

    /** Returns the canned results for a test type, or an empty list if the type is unsupported. */
    public List<LisResultRequest.TestResultItem> getMockResults(String testType) {
        return mockData.getOrDefault(testType, List.of());
    }

    /** Returns the set of test type names this provider has mock data for — also used as the allow-list when creating a lab order. */
    public Set<String> getSupportedTestTypes() {
        return mockData.keySet();
    }
}
