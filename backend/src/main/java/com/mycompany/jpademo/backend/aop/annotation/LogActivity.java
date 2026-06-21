package com.mycompany.jpademo.backend.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD) // Chỉ được dán lên trên các Hàm (Method)
@Retention(RetentionPolicy.RUNTIME) // Tồn tại trong lúc ứng dụng đang chạy
public @interface LogActivity {

    String action();
    String targetType();
    String description() default "";
}
