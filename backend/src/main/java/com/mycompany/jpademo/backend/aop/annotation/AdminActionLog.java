package com.mycompany.jpademo.backend.aop.annotation;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AdminActionLog {
    String action();

    String targetType();
}
