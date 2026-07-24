package com.mycompany.jpademo.backend.converter;

import com.mycompany.jpademo.backend.enums.UserStatus;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter implements AttributeConverter<UserStatus, String> {

    @Override
    public String convertToDatabaseColumn(UserStatus attribute) {
        return attribute != null ? attribute.name() : null;
    }

    @Override
    public UserStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return UserStatus.ACTIVE;
        }
        String cleanData = dbData.trim().toUpperCase();
        if ("BANNED".equals(cleanData) || "BLOCKED".equals(cleanData) || "LOCKED".equals(cleanData)) {
            return UserStatus.BANNED;
        }
        return UserStatus.ACTIVE;
    }
}
