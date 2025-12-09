package com.utility.masking.util;

import com.utility.masking.service.MaskingService;
import org.springframework.stereotype.Component;

/**
 * Utility helper for masking data in logs
 * 
 * Use this when you want explicit control over masking in your log statements
 */
@Component
public class MaskingHelper {

    private static MaskingService maskingService;

    public MaskingHelper(MaskingService maskingService) {
        MaskingHelper.maskingService = maskingService;
    }

    /**
     * Masks sensitive data in the given object
     * 
     * Usage:
     * <pre>
     * log.info("User data: {}", MaskingHelper.mask(user));
     * </pre>
     * 
     * @param obj the object to mask
     * @return masked string representation
     */
    public static String mask(Object obj) {
        if (maskingService == null) {
            // Fallback if Spring context not initialized
            return obj != null ? obj.toString() : "null";
        }
        return maskingService.maskData(obj);
    }

    /**
     * Masks multiple objects
     * 
     * @param objects the objects to mask
     * @return array of masked string representations
     */
    public static String[] mask(Object... objects) {
        String[] masked = new String[objects.length];
        for (int i = 0; i < objects.length; i++) {
            masked[i] = mask(objects[i]);
        }
        return masked;
    }
}
