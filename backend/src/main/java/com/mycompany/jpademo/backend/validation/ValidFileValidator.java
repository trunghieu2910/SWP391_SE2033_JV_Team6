package com.mycompany.jpademo.backend.validation;

import com.mycompany.jpademo.backend.util.SecureFileUploadUtil;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

public class ValidFileValidator implements ConstraintValidator<ValidFile, MultipartFile> {

    private boolean required;
    private long maxSize;

    @Override
    public void initialize(ValidFile annotation) {
        this.required = annotation.required();
        this.maxSize = annotation.maxSizeBytes();
    }

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext ctx) {
        boolean isEmpty = (file == null || file.isEmpty());

        if (isEmpty) {
            return !required;
        }

        return SecureFileUploadUtil.isValidSizeAndType(file, maxSize);
    }
}