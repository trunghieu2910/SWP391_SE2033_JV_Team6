package com.mycompany.jpademo.backend.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ValidFileValidator.class)
public @interface ValidFile {
    String message() default "File không hợp lệ";
    boolean required() default true;
    long maxSizeBytes() default 5 * 1024 * 1024;
    String[] allowedExtensions() default {".pdf", ".png", ".jpg", ".jpeg"};

    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}