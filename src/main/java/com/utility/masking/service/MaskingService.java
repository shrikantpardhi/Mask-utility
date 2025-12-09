package com.utility.masking.service;

import com.utility.masking.annotation.Sensitive;
import com.utility.masking.strategy.MaskStrategy;
import com.utility.masking.masker.DataMasker;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;

/**
 * Service responsible for masking sensitive data in objects
 */
@Service
public class MaskingService {

    /**
     * Masks sensitive fields in the given object
     * @param obj the object to mask
     * @return a string representation with masked sensitive fields
     */
    public String maskData(Object obj) {
        if (obj == null) {
            return "null";
        }

        if (isPrimitiveOrWrapper(obj.getClass())) {
            return obj.toString();
        }

        if (obj instanceof String) {
            return (String) obj;
        }

        if (obj instanceof Collection) {
            return maskCollection((Collection<?>) obj);
        }

        if (obj instanceof Map) {
            return maskMap((Map<?, ?>) obj);
        }

        if (obj.getClass().isArray()) {
            return maskArray(obj);
        }

        return maskObject(obj);
    }

    /**
     * Masks sensitive fields in a plain object
     */
    private String maskObject(Object obj) {
        StringBuilder sb = new StringBuilder();
        Class<?> clazz = obj.getClass();
        sb.append(clazz.getSimpleName()).append("{");

        Field[] fields = getAllFields(clazz);
        boolean first = true;

        for (Field field : fields) {
            // Skip static and synthetic fields
            if (java.lang.reflect.Modifier.isStatic(field.getModifiers()) || 
                field.isSynthetic()) {
                continue;
            }

            field.setAccessible(true);
            try {
                Object value = field.get(obj);
                
                if (!first) {
                    sb.append(", ");
                }
                first = false;

                sb.append(field.getName()).append("=");

                if (field.isAnnotationPresent(Sensitive.class)) {
                    Sensitive sensitive = field.getAnnotation(Sensitive.class);
                    sb.append(maskValue(value, sensitive));
                } else if (value != null && !isPrimitiveOrWrapper(value.getClass()) 
                          && !(value instanceof String)) {
                    // Recursively mask nested objects
                    sb.append(maskData(value));
                } else {
                    sb.append(value);
                }
            } catch (IllegalAccessException e) {
                sb.append("inaccessible");
            }
        }

        sb.append("}");
        return sb.toString();
    }

    /**
     * Masks a value based on the Sensitive annotation configuration
     */
    private String maskValue(Object value, Sensitive sensitive) {
        if (value == null) {
            return "null";
        }

        String strValue = value.toString();
        MaskStrategy strategy = sensitive.strategy();
        char maskChar = sensitive.maskChar();

        return switch (strategy) {
            case FULL -> maskFull(strValue, maskChar);
            case FIRST_LAST -> maskFirstLast(strValue, maskChar);
            case LAST_FOUR -> maskLastFour(strValue, maskChar);
            case CUSTOM -> maskCustom(strValue, sensitive);
        };
    }

    /**
     * Masks all characters
     */
    private String maskFull(String value, char maskChar) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return String.valueOf(maskChar).repeat(value.length());
    }

    /**
     * Keeps first and last character, masks the rest
     */
    private String maskFirstLast(String value, char maskChar) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() <= 2) {
            return value;
        }
        
        String masked = String.valueOf(maskChar).repeat(value.length() - 2);
        return value.charAt(0) + masked + value.charAt(value.length() - 1);
    }

    /**
     * Keeps last 4 characters, masks the rest
     */
    private String maskLastFour(String value, char maskChar) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (value.length() <= 4) {
            return value;
        }
        
        String masked = String.valueOf(maskChar).repeat(value.length() - 4);
        return masked + value.substring(value.length() - 4);
    }

    /**
     * Applies custom masking logic
     */
    private String maskCustom(String value, Sensitive sensitive) {
        try {
            Class<? extends DataMasker> maskerClass = sensitive.customMasker();
            DataMasker masker = maskerClass.getDeclaredConstructor().newInstance();
            return masker.mask(value, sensitive.maskChar());
        } catch (Exception e) {
            // Fallback to full masking if custom masker fails
            return maskFull(value, sensitive.maskChar());
        }
    }

    /**
     * Masks sensitive data in collections
     */
    private String maskCollection(Collection<?> collection) {
        if (collection.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (Object item : collection) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(maskData(item));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Masks sensitive data in maps
     */
    private String maskMap(Map<?, ?> map) {
        if (map.isEmpty()) {
            return "{}";
        }

        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!first) {
                sb.append(", ");
            }
            first = false;
            sb.append(entry.getKey()).append("=").append(maskData(entry.getValue()));
        }
        sb.append("}");
        return sb.toString();
    }

    /**
     * Masks sensitive data in arrays
     */
    private String maskArray(Object array) {
        int length = java.lang.reflect.Array.getLength(array);
        if (length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            Object item = java.lang.reflect.Array.get(array, i);
            sb.append(maskData(item));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Gets all fields including inherited ones
     */
    private Field[] getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        while (clazz != null && clazz != Object.class) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields.toArray(new Field[0]);
    }

    /**
     * Checks if a class is a primitive or wrapper type
     */
    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive() || 
               clazz == String.class ||
               clazz == Integer.class ||
               clazz == Long.class ||
               clazz == Double.class ||
               clazz == Float.class ||
               clazz == Boolean.class ||
               clazz == Character.class ||
               clazz == Byte.class ||
               clazz == Short.class ||
               Number.class.isAssignableFrom(clazz);
    }
    }
