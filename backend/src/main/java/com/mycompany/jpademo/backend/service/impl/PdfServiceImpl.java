package com.mycompany.jpademo.backend.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.mycompany.jpademo.backend.dto.response.MedicalRecordDetailResponse;
import com.mycompany.jpademo.backend.entity.Prescription;
import com.mycompany.jpademo.backend.entity.PrescriptionDetail;
import com.mycompany.jpademo.backend.repository.PrescriptionRepository;
import com.mycompany.jpademo.backend.service.interfaces.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final PrescriptionRepository prescriptionRepository;

    @Override
    public byte[] generateMedicalRecordPdf(MedicalRecordDetailResponse record, boolean isPatientView) {
        Prescription prescription = prescriptionRepository.findBySessionSessionId(record.getSessionID()).orElse(null);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 36, 36, 36, 36);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // 1. Setup fonts supporting Vietnamese Unicode
            Font titleFont = getVietnameseFont(18, Font.BOLD, new Color(16, 3, 87));
            Font subtitleFont = getVietnameseFont(10, Font.ITALIC, new Color(100, 116, 139));
            Font sectionHeaderFont = getVietnameseFont(11, Font.BOLD, Color.WHITE);
            Font boldFont = getVietnameseFont(9, Font.BOLD, new Color(15, 23, 42));
            Font normalFont = getVietnameseFont(9, Font.NORMAL, new Color(15, 23, 42));
            Font smallFont = getVietnameseFont(8, Font.NORMAL, new Color(100, 116, 139));
            Font italicFont = getVietnameseFont(8, Font.ITALIC, new Color(100, 116, 139));

            // 2. Add Header info
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{6f, 4f});
            
            // Left Header (Hospital/Clinic info)
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.addElement(new Paragraph("HỆ THỐNG PHÒNG KHÁM ĐA KHOA CHẤN ĐOÁN THÔNG MINH", getVietnameseFont(8, Font.BOLD, new Color(16, 3, 87))));
            leftCell.addElement(new Paragraph("Địa chỉ: Khu Công nghệ cao Hòa Lạc, Thạch Thất, Hà Nội", getVietnameseFont(7, Font.NORMAL, new Color(100, 116, 139))));
            leftCell.addElement(new Paragraph("Hotline: 1900 1234 - Email: contact@smartclinic.vn", getVietnameseFont(7, Font.NORMAL, new Color(100, 116, 139))));
            headerTable.addCell(leftCell);

            // Right Header (Record Code & Export Date)
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            Paragraph codePara = new Paragraph("Mã bệnh án: #S" + String.format("%04d", record.getSessionID()), getVietnameseFont(9, Font.BOLD, new Color(239, 68, 68)));
            codePara.setAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(codePara);
            Paragraph datePara = new Paragraph("Ngày xuất file: " + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), getVietnameseFont(7, Font.NORMAL, new Color(100, 116, 139)));
            datePara.setAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(datePara);
            headerTable.addCell(rightCell);

            document.add(headerTable);

            // Divider Line
            addDivider(document);

            // Title
            Paragraph title = new Paragraph("HỒ SƠ BỆNH ÁN CHI TIẾT", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(10f);
            title.setSpacingAfter(15f);
            document.add(title);

            // I. THÔNG TIN BỆNH NHÂN
            addSectionHeader(document, "I. THÔNG TIN BỆNH NHÂN (PATIENT INFORMATION)", sectionHeaderFont);
            PdfPTable patientTable = new PdfPTable(4);
            patientTable.setWidthPercentage(100);
            patientTable.setWidths(new float[]{2f, 3f, 2f, 3f});
            patientTable.setSpacingBefore(5f);
            patientTable.setSpacingAfter(10f);

            patientTable.addCell(createLabelCell("Họ và tên:", boldFont));
            patientTable.addCell(createValueCell(record.getPatientFullName(), normalFont));
            patientTable.addCell(createLabelCell("Số CCCD:", boldFont));
            patientTable.addCell(createValueCell(record.getPatientNationalID(), normalFont));

            patientTable.addCell(createLabelCell("Ngày sinh:", boldFont));
            String dobStr = record.getPatientDob() != null ? new SimpleDateFormat("dd/MM/yyyy").format(record.getPatientDob()) : "—";
            patientTable.addCell(createValueCell(dobStr, normalFont));
            patientTable.addCell(createLabelCell("Giới tính:", boldFont));
            String genderStr = "Female".equalsIgnoreCase(record.getPatientGender()) ? "Nữ" : "Nam";
            patientTable.addCell(createValueCell(genderStr, normalFont));

            patientTable.addCell(createLabelCell("Số điện thoại:", boldFont));
            patientTable.addCell(createValueCell(record.getPatientPhone(), normalFont));
            patientTable.addCell(createLabelCell("Địa chỉ:", boldFont));
            patientTable.addCell(createValueCell(record.getPatientAddress(), normalFont));

            document.add(patientTable);

            // II. THÔNG TIN PHIÊN KHÁM
            addSectionHeader(document, "II. THÔNG TIN PHIÊN KHÁM (VISIT INFORMATION)", sectionHeaderFont);
            PdfPTable sessionTable = new PdfPTable(4);
            sessionTable.setWidthPercentage(100);
            sessionTable.setWidths(new float[]{2f, 3f, 2f, 3f});
            sessionTable.setSpacingBefore(5f);
            sessionTable.setSpacingAfter(10f);

            sessionTable.addCell(createLabelCell("Mã phiên khám:", boldFont));
            sessionTable.addCell(createValueCell("#S" + String.format("%04d", record.getSessionID()), normalFont));
            sessionTable.addCell(createLabelCell("Bác sĩ phụ trách:", boldFont));
            sessionTable.addCell(createValueCell(record.getDoctorFullName(), normalFont));

            sessionTable.addCell(createLabelCell("Cân nặng:", boldFont));
            String wStr = record.getWeight() != null ? record.getWeight() + " kg" : "—";
            sessionTable.addCell(createValueCell(wStr, normalFont));
            sessionTable.addCell(createLabelCell("Chiều cao:", boldFont));
            String hStr = record.getHeight() != null ? record.getHeight() + " cm" : "—";
            sessionTable.addCell(createValueCell(hStr, normalFont));

            sessionTable.addCell(createLabelCell("Chỉ số BMI:", boldFont));
            String bmiStr = "—";
            if (record.getWeight() != null && record.getHeight() != null && record.getHeight() > 0) {
                double heightM = record.getHeight() / 100.0;
                double bmi = record.getWeight() / (heightM * heightM);
                bmiStr = String.format("%.1f", bmi);
            }
            sessionTable.addCell(createValueCell(bmiStr, normalFont));
            sessionTable.addCell(createLabelCell("Ngày lập khám:", boldFont));
            String createdStr = record.getCreatedAt() != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(record.getCreatedAt()) : "—";
            sessionTable.addCell(createValueCell(createdStr, normalFont));

            document.add(sessionTable);

            // III. KẾT QUẢ TRIỆU CHỨNG
            addSectionHeader(document, "III. KẾT QUẢ TRIỆU CHỨNG (SYMPTOMS RESULTS)", sectionHeaderFont);
            if (record.getSymptomResultID() != null) {
                PdfPTable symptomTable = new PdfPTable(4);
                symptomTable.setWidthPercentage(100);
                symptomTable.setWidths(new float[]{2.5f, 2.5f, 2.5f, 2.5f});
                symptomTable.setSpacingBefore(5f);
                symptomTable.setSpacingAfter(8f);

                symptomTable.addCell(createLabelCell("Mã triệu chứng:", boldFont));
                symptomTable.addCell(createValueCell("#SR" + record.getSymptomResultID(), normalFont));
                symptomTable.addCell(createLabelCell("Trạng thái triệu chứng:", boldFont));
                symptomTable.addCell(createValueCell(record.getSymptomResultStatus(), normalFont));

                symptomTable.addCell(createLabelCell("Mãn kinh:", boldFont));
                symptomTable.addCell(createValueCell(record.getMenopauseStatus(), normalFont));
                symptomTable.addCell(createLabelCell("Thời gian triệu chứng:", boldFont));
                symptomTable.addCell(createValueCell(record.getSymptomDuration(), normalFont));

                symptomTable.addCell(createLabelCell("Triệu chứng nặng dần:", boldFont));
                String progStr = record.getSymptomProgressing() != null ? (record.getSymptomProgressing() ? "Có - Đang tăng" : "Không - Ổn định") : "—";
                symptomTable.addCell(createValueCell(progStr, normalFont));
                symptomTable.addCell(createLabelCell("", boldFont));
                symptomTable.addCell(createValueCell("", normalFont));

                document.add(symptomTable);

                // List of symptoms as tags
                if (record.getSymptoms() != null && !record.getSymptoms().isEmpty()) {
                    Paragraph listTitle = new Paragraph("Danh sách triệu chứng ghi nhận:", boldFont);
                    listTitle.setSpacingAfter(3f);
                    document.add(listTitle);
                    
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < record.getSymptoms().size(); i++) {
                        sb.append(record.getSymptoms().get(i).getSymptomName());
                        if (i < record.getSymptoms().size() - 1) {
                            sb.append(", ");
                        }
                    }
                    Paragraph symList = new Paragraph(sb.toString(), normalFont);
                    symList.setSpacingAfter(10f);
                    document.add(symList);
                } else {
                    Paragraph symList = new Paragraph("Không ghi nhận danh sách triệu chứng chi tiết.", italicFont);
                    symList.setSpacingAfter(10f);
                    document.add(symList);
                }
            } else {
                Paragraph noSymptoms = new Paragraph("Chưa ghi nhận triệu chứng lâm sàng cho phiên khám này.", italicFont);
                noSymptoms.setSpacingBefore(5f);
                noSymptoms.setSpacingAfter(10f);
                document.add(noSymptoms);
            }

            // IV. KẾT QUẢ XÉT NGHIỆM
            addSectionHeader(document, "IV. KẾT QUẢ XÉT NGHIỆM (LABORATORY TESTS)", sectionHeaderFont);
            if (record.getLabTests() != null && !record.getLabTests().isEmpty()) {
                for (MedicalRecordDetailResponse.LabTestDTO lab : record.getLabTests()) {
                    Paragraph labTitle = new Paragraph("Xét nghiệm: " + (lab.getTestName() != null ? lab.getTestName() : "—") 
                            + " [Mã: #LR" + lab.getLabResultID() + "]", boldFont);
                    labTitle.setSpacingBefore(4f);
                    labTitle.setSpacingAfter(2f);
                    document.add(labTitle);

                    String dateStr = lab.getTestedAt() != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(lab.getTestedAt()) : "—";
                    Paragraph labMeta = new Paragraph("Ngày chỉ định: " + dateStr + " | Trạng thái: " + lab.getStatus(), smallFont);
                    labMeta.setSpacingAfter(4f);
                    document.add(labMeta);

                    if (lab.getParameters() != null && !lab.getParameters().isEmpty()) {
                        PdfPTable paramTable = new PdfPTable(4);
                        paramTable.setWidthPercentage(100);
                        paramTable.setWidths(new float[]{1f, 5f, 2f, 2f});
                        paramTable.setSpacingAfter(10f);

                        // Header cells
                        paramTable.addCell(createTableHeaderCell("#", boldFont));
                        paramTable.addCell(createTableHeaderCell("Tên thông số", boldFont));
                        paramTable.addCell(createTableHeaderCell("Đơn vị", boldFont));
                        paramTable.addCell(createTableHeaderCell("Giá trị", boldFont));

                        List<MedicalRecordDetailResponse.ParamDTO> params = lab.getParameters();
                        for (int i = 0; i < params.size(); i++) {
                            MedicalRecordDetailResponse.ParamDTO param = params.get(i);
                            paramTable.addCell(createTableCell(String.valueOf(i + 1), normalFont, Element.ALIGN_CENTER));
                            paramTable.addCell(createTableCell(param.getParamName(), normalFont, Element.ALIGN_LEFT));
                            paramTable.addCell(createTableCell(param.getUnit(), normalFont, Element.ALIGN_CENTER));
                            paramTable.addCell(createTableCell(param.getValue(), boldFont, Element.ALIGN_CENTER));
                        }
                        document.add(paramTable);
                    } else {
                        Paragraph noParams = new Paragraph("Không có thông số chi tiết cho xét nghiệm này.", italicFont);
                        noParams.setSpacingAfter(8f);
                        document.add(noParams);
                    }
                }
            } else {
                Paragraph noLab = new Paragraph("Không có chỉ định xét nghiệm lâm sàng nào.", italicFont);
                noLab.setSpacingBefore(5f);
                noLab.setSpacingAfter(10f);
                document.add(noLab);
            }

            // V. HÌNH ẢNH Y KHOA
            addSectionHeader(document, "V. HÌNH ẢNH Y KHOA (MEDICAL IMAGES)", sectionHeaderFont);
            if (record.getMedicalImages() != null && !record.getMedicalImages().isEmpty()) {
                for (MedicalRecordDetailResponse.ImageDTO img : record.getMedicalImages()) {
                    Paragraph imgTitle = new Paragraph("Loại hình ảnh: " + (img.getImageType() != null ? img.getImageType() : "—") 
                            + " [Mã: #MI" + img.getMedicalImageID() + "]", boldFont);
                    imgTitle.setSpacingBefore(4f);
                    imgTitle.setSpacingAfter(2f);
                    document.add(imgTitle);

                    String dateStr = img.getCreatedAt() != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(img.getCreatedAt()) : "—";
                    Paragraph imgMeta = new Paragraph("Ngày chỉ định: " + dateStr + " | Trạng thái: " + img.getStatus(), smallFont);
                    imgMeta.setSpacingAfter(4f);
                    document.add(imgMeta);

                    if (img.getDetails() != null && !img.getDetails().isEmpty()) {
                        for (int i = 0; i < img.getDetails().size(); i++) {
                            MedicalRecordDetailResponse.ImageDetailDTO detail = img.getDetails().get(i);
                            // PHÂN QUYỀN BÁC SĨ / BỆNH NHÂN ĐỐI VỚI HÌNH ẢNH Y KHOA:
                            if (isPatientView) {
                                // 1. NẾU LÀ BỆNH NHÂN: Chỉ xuất ra "Ảnh kết luận" (là ảnh đã được bác sĩ/KTV khoanh vùng, đánh dấu).
                                // Bệnh nhân không được xem Ảnh gốc hay Ảnh AI.
                                String url = detail.getImgResultConclusion();
                                if (url != null && !url.trim().isEmpty()) {
                                    addImageToPdf(document, url, "Ảnh kết luận", normalFont, italicFont);
                                }
                            } else {
                                // 2. NẾU LÀ BÁC SĨ: Bác sĩ được quyền xem toàn bộ tất cả các loại ảnh (Ảnh gốc, Ảnh AI, Ảnh kết luận).
                                if (detail.getImageUrl() != null && !detail.getImageUrl().trim().isEmpty()) {
                                    addImageToPdf(document, detail.getImageUrl(), "Ảnh gốc", normalFont, italicFont);
                                }
                                if (detail.getAiImageUrl() != null && !detail.getAiImageUrl().trim().isEmpty()) {
                                    addImageToPdf(document, detail.getAiImageUrl(), "Ảnh AI dự đoán", normalFont, italicFont);
                                }
                                if (detail.getImgResultConclusion() != null && !detail.getImgResultConclusion().trim().isEmpty()) {
                                    addImageToPdf(document, detail.getImgResultConclusion(), "Ảnh KTV khoanh (Kết luận)", normalFont, italicFont);
                                }
                            }
                        }
                    } else {
                        Paragraph noFiles = new Paragraph("Chưa tải file ảnh nào lên.", italicFont);
                        noFiles.setSpacingAfter(8f);
                        document.add(noFiles);
                    }
                }
            } else {
                Paragraph noImg = new Paragraph("Không có chỉ định chẩn đoán hình ảnh nào.", italicFont);
                noImg.setSpacingBefore(5f);
                noImg.setSpacingAfter(10f);
                document.add(noImg);
            }

            // VI. KẾT LUẬN BÁC SĨ (Conclusion Box)
            addConclusionCard(document, record, boldFont, normalFont, italicFont);

            // VII. ĐƠN THUỐC BÁC SĨ KÊ
            addSectionHeader(document, "VII. ĐƠN THUỐC BÁC SĨ KÊ (PRESCRIPTION)", sectionHeaderFont);
            
            // PHÂN QUYỀN BÁC SĨ / BỆNH NHÂN ĐỐI VỚI ĐƠN THUỐC:
            // - Nếu là Bệnh nhân (isPatientView = true) và bác sĩ CHƯA công bố hồ sơ (isShared = false): Ẩn bảng đơn thuốc đi.
            if (isPatientView && (record.getIsShared() == null || !record.getIsShared())) {
                Paragraph noPrescription = new Paragraph("Bác sĩ chưa công bố đơn thuốc cho ca chẩn đoán này.", italicFont);
                noPrescription.setSpacingBefore(5f);
                noPrescription.setSpacingAfter(10f);
                document.add(noPrescription);
            } else if (prescription != null && prescription.getDetails() != null && !prescription.getDetails().isEmpty()) {
                Paragraph rxTitle = new Paragraph("Mã đơn thuốc: " + prescription.getPrescriptionCode() + 
                        " | Ngày kê: " + (prescription.getPrescriptionDate() != null ? prescription.getPrescriptionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "—"), boldFont);
                rxTitle.setSpacingBefore(4f);
                rxTitle.setSpacingAfter(6f);
                document.add(rxTitle);

                PdfPTable rxTable = new PdfPTable(5);
                rxTable.setWidthPercentage(100);
                rxTable.setWidths(new float[]{1f, 4f, 2f, 2f, 3f});
                rxTable.setSpacingAfter(10f);

                rxTable.addCell(createTableHeaderCell("#", boldFont));
                rxTable.addCell(createTableHeaderCell("Tên thuốc", boldFont));
                rxTable.addCell(createTableHeaderCell("Số lượng", boldFont));
                rxTable.addCell(createTableHeaderCell("Đơn vị", boldFont));
                rxTable.addCell(createTableHeaderCell("Hướng dẫn sử dụng", boldFont));

                List<PrescriptionDetail> rxDetails = prescription.getDetails();
                for (int i = 0; i < rxDetails.size(); i++) {
                    PrescriptionDetail detail = rxDetails.get(i);
                    rxTable.addCell(createTableCell(String.valueOf(i + 1), normalFont, Element.ALIGN_CENTER));
                    rxTable.addCell(createTableCell(detail.getDrug() != null ? detail.getDrug().getDrugName() : "—", normalFont, Element.ALIGN_LEFT));
                    rxTable.addCell(createTableCell(String.valueOf(detail.getQuantityPrescribed()), boldFont, Element.ALIGN_CENTER));
                    rxTable.addCell(createTableCell(detail.getDispenseUnit(), normalFont, Element.ALIGN_CENTER));
                    rxTable.addCell(createTableCell(detail.getInstruction(), normalFont, Element.ALIGN_LEFT));
                }
                document.add(rxTable);
            } else {
                Paragraph noPrescription = new Paragraph("Không có đơn thuốc nào được kê cho phiên khám này.", italicFont);
                noPrescription.setSpacingBefore(5f);
                noPrescription.setSpacingAfter(10f);
                document.add(noPrescription);
            }
            // PHÂN QUYỀN BÁC SĨ / BỆNH NHÂN ĐỐI VỚI CHỮ KÝ:
            // Chỉ thêm khung chữ ký nếu người xuất file là Bác sĩ (!isPatientView). Bệnh nhân không có phần này.
            if (!isPatientView) {
                document.add(new Paragraph(" ", normalFont));
                document.add(new Paragraph(" ", normalFont));

                PdfPTable doctorSignatureTable = new PdfPTable(2);
                doctorSignatureTable.setWidthPercentage(100);
                doctorSignatureTable.setWidths(new float[]{6f, 4f});

                PdfPCell emptyCell = new PdfPCell();
                emptyCell.setBorder(Rectangle.NO_BORDER);
                doctorSignatureTable.addCell(emptyCell);

                PdfPCell signCell = new PdfPCell();
                signCell.setBorder(Rectangle.NO_BORDER);
                signCell.setHorizontalAlignment(Element.ALIGN_LEFT);

                Paragraph docSignTitle = new Paragraph("BÁC SĨ PHỤ TRÁCH", boldFont);
                docSignTitle.setSpacingAfter(40f);
                signCell.addElement(docSignTitle);

                signCell.addElement(new Paragraph("Chữ ký: ................................", normalFont));

                String docName = record.getDoctorFullName() != null ? record.getDoctorFullName() : "....................";
                Paragraph namePara = new Paragraph("BS. " + docName, normalFont);
                namePara.setSpacingBefore(4f);
                signCell.addElement(namePara);

                String dateStr;
                if (prescription != null && prescription.getPrescriptionDate() != null) {
                    dateStr = prescription.getPrescriptionDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                } else if (record.getReviewedAt() != null) {
                    dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(record.getReviewedAt());
                } else {
                    dateStr = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
                }
                Paragraph signDatePara = new Paragraph("Ngày kê: " + dateStr, normalFont);
                signDatePara.setSpacingBefore(4f);
                signCell.addElement(signDatePara);

                doctorSignatureTable.addCell(signCell);
                document.add(doctorSignatureTable);
            }
            document.close();
        } catch (DocumentException e) {
            throw new RuntimeException("Error during PDF generation: " + e.getMessage(), e);
        }

        return out.toByteArray();
    }

    private Font getVietnameseFont(int size, int style, Color color) {
        try {
            // Try Windows standard path first (most likely for development/Windows environment)
            String winFontPath = "C:/Windows/Fonts/arial.ttf";
            if (new java.io.File(winFontPath).exists()) {
                BaseFont bf = BaseFont.createFont(winFontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                return new Font(bf, size, style, color);
            }
            
            // Try system standard path fallbacks for other OS
            String[] paths = {
                "C:/Windows/Fonts/times.ttf",
                "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
                "/System/Library/Fonts/Helvetica.ttc"
            };
            for (String path : paths) {
                if (new java.io.File(path).exists()) {
                    BaseFont bf = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    return new Font(bf, size, style, color);
                }
            }

            // Fallback using FontFactory directory registration
            FontFactory.registerDirectories();
            Font f = FontFactory.getFont("Arial", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, size, style, color);
            if (f != null && f.getBaseFont() != null) {
                return f;
            }
        } catch (Exception e) {
            System.err.println("Could not load Vietnamese Unicode font: " + e.getMessage());
        }
        // Last fallback to default lowagie font (which might not display Vietnamese diacritics but won't crash)
        return FontFactory.getFont(FontFactory.HELVETICA, size, style, color);
    }

    private void addDivider(Document document) throws DocumentException {
        Paragraph divider = new Paragraph(" ", getVietnameseFont(4, Font.NORMAL, Color.GRAY));
        divider.setSpacingBefore(2f);
        divider.setSpacingAfter(2f);
        
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.BOTTOM);
        cell.setBorderWidth(1f);
        cell.setBorderColor(new Color(203, 213, 225)); // slate 300
        cell.setFixedHeight(1f);
        
        table.addCell(cell);
        document.add(table);
        document.add(divider);
    }

    private void addSectionHeader(Document document, String title, Font font) throws DocumentException {
        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(8f);
        table.setSpacingAfter(6f);
        
        PdfPCell cell = new PdfPCell(new Phrase(title, font));
        cell.setBackgroundColor(new Color(16, 3, 87)); // Primary blue
        cell.setPaddingTop(5f);
        cell.setPaddingBottom(5f);
        cell.setPaddingLeft(6f);
        cell.setPaddingRight(6f);
        cell.setBorder(Rectangle.NO_BORDER);
        
        table.addCell(cell);
        document.add(table);
    }

    private PdfPCell createLabelCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3f);
        cell.setPaddingLeft(0f);
        return cell;
    }

    private PdfPCell createValueCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "—", font));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(3f);
        cell.setPaddingLeft(0f);
        return cell;
    }

    private PdfPCell createTableHeaderCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(241, 245, 249)); // light gray slate #f1f5f9
        cell.setBorderColor(new Color(226, 232, 240)); // #e2e8f0
        cell.setBorderWidth(1f);
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell createTableCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "—", font));
        cell.setBorderColor(new Color(226, 232, 240)); // #e2e8f0
        cell.setBorderWidth(1f);
        cell.setPadding(5f);
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private void addConclusionCard(Document document, MedicalRecordDetailResponse record, Font boldFont, Font normalFont, Font italicFont) throws DocumentException {
        PdfPTable card = new PdfPTable(1);
        card.setWidthPercentage(100);
        card.setSpacingBefore(10f);
        card.setSpacingAfter(10f);
        
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(248, 250, 252)); // Light slate gray #f8fafc
        cell.setBorderColor(new Color(226, 232, 240)); // Border #e2e8f0
        cell.setBorderWidth(1f);
        cell.setPadding(10f);
        
        Paragraph title = new Paragraph("VI. KẾT LUẬN CỦA BÁC SĨ (DOCTOR'S DIAGNOSIS & CONCLUSIONS)", getVietnameseFont(10, Font.BOLD, new Color(16, 3, 87)));
        title.setSpacingAfter(8f);
        cell.addElement(title);
        
        if (record.getReviewID() != null) {
            cell.addElement(new Paragraph("Chẩn đoán cuối cùng:", boldFont));
            Paragraph diagPara = new Paragraph(record.getFinalDiagnosis(), normalFont);
            diagPara.setSpacingAfter(5f);
            cell.addElement(diagPara);
            
            cell.addElement(new Paragraph("Phác đồ điều trị & Đơn thuốc:", boldFont));
            Paragraph treatPara = new Paragraph(record.getTreatmentPlan(), normalFont);
            treatPara.setSpacingAfter(5f);
            cell.addElement(treatPara);
            
            cell.addElement(new Paragraph("Hướng dẫn & Lời khuyên:", boldFont));
            Paragraph advicePara = new Paragraph(record.getDoctorAdvice(), normalFont);
            advicePara.setSpacingAfter(5f);
            cell.addElement(advicePara);
            
            if (record.getNote() != null && !record.getNote().trim().isEmpty()) {
                cell.addElement(new Paragraph("Ghi chú thêm:", boldFont));
                Paragraph notePara = new Paragraph(record.getNote(), normalFont);
                notePara.setSpacingAfter(5f);
                cell.addElement(notePara);
            }
            
            String reviewer = record.getReviewedByDoctorName();
            String reviewDateStr = record.getReviewedAt() != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm").format(record.getReviewedAt()) : "—";
            Paragraph footerPara = new Paragraph("Kết luận bởi Bác sĩ: " + (reviewer != null ? reviewer : "—") + " vào lúc " + reviewDateStr, italicFont);
            footerPara.setAlignment(Element.ALIGN_RIGHT);
            cell.addElement(footerPara);
        } else {
            Paragraph noReview = new Paragraph("Chưa có kết luận chẩn đoán chính thức từ bác sĩ cho phiên khám này.", italicFont);
            cell.addElement(noReview);
        }
        
        card.addCell(cell);
        document.add(card);
    }
    
    private void addImageToPdf(Document document, String url, String label, Font normalFont, Font italicFont) throws DocumentException {
        if (url == null || url.trim().isEmpty()) return;


        try {
            String imagePath = url;
            if (url.startsWith("/uploads/")) {
                imagePath = "uploads/" + url.substring(9);
            }

            java.io.File imgFile = new java.io.File(imagePath);
            if (imgFile.exists()) {
                // Dùng bảng 1 cột để giữ nhãn + ảnh luôn liền nhau, không bị tách trang
                PdfPTable wrapper = new PdfPTable(1);
                wrapper.setWidthPercentage(100);
                wrapper.setSpacingBefore(10f);
                wrapper.setSpacingAfter(10f);
                wrapper.setKeepTogether(true);

                PdfPCell cell = new PdfPCell();
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPadding(0);

                // Nhãn nằm trên ảnh
                Paragraph labelPara = new Paragraph(label, normalFont);
                labelPara.setSpacingAfter(5f);
                cell.addElement(labelPara);

                // Ảnh nằm dưới nhãn
                Image image = Image.getInstance(imgFile.getAbsolutePath());
                image.scaleToFit(480f, 380f);
                image.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(image);

                wrapper.addCell(cell);
                document.add(wrapper);
            } else {
                Paragraph labelPara = new Paragraph(label, normalFont);
                labelPara.setSpacingBefore(6f);
                document.add(labelPara);
                Paragraph errorPara = new Paragraph("   (Không tìm thấy file hình ảnh thực tế trên hệ thống)", italicFont);
                errorPara.setSpacingAfter(8f);
                document.add(errorPara);
            }
        } catch (Exception ex) {
            System.err.println("Could not add image to PDF: " + ex.getMessage());
        }
    }
}
