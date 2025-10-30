package org.ikigaidigital.util;

import lombok.extern.slf4j.Slf4j;
import org.ikigaidigital.entity.TimeDepositEntity;

import java.lang.reflect.Field;

@Slf4j
public final class PropertiesUtil {

    private PropertiesUtil() {
    }

    public static void updateProperty(TimeDepositEntity timeDepositEntity, String fieldName, Object newValue) {
        try {
            Field field = timeDepositEntity.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(timeDepositEntity, newValue);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            log.error("Failed to update property:" + fieldName + " with value: " + newValue + ". Error={}: " + e.getMessage());
        }
    }
}
