package com.utility.masking.converter;

import com.utility.masking.service.MaskingService;
import org.springframework.stereotype.Component;

/**
 * Message converter that automatically masks sensitive data when objects are logged
 * 
 * This integrates with SLF4J's toString() mechanism to ensure that whenever
 * an object with @Sensitive fields is logged, those fields are automatically masked.
 */
@Component
public class MaskingMessageConverter {

    private final MaskingService maskingService;

    public MaskingMessageConverter(MaskingService maskingService) {
        this.maskingService = maskingService;
    }

    /**
     * Converts an object to a masked string representation
     * 
     * @param obj the object to convert
     * @return masked string representation
     */
    public String convert(Object obj) {
        return maskingService.maskData(obj);
    }
}
