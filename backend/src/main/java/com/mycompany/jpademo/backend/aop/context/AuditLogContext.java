package com.mycompany.jpademo.backend.aop.context;

public class AuditLogContext {

    private static final ThreadLocal<Integer> targetIdHolder = new ThreadLocal<>();

    public static void setTargetId(Integer id) {
        targetIdHolder.set(id);
    }

    public static Integer getTargetId() {
        return targetIdHolder.get();
    }

    public static void clear() {
        targetIdHolder.remove();
    }
}
