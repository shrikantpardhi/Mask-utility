package com.utility.masking.masker;

/**
 * Interface for implementing custom masking strategies
 */
public interface DataMasker {
    
    /**
     * Masks the given value using custom logic
     * 
     * @param value the value to mask
     * @param maskChar the character to use for masking
     * @return the masked value
     */
    String mask(String value, char maskChar);
    
    /**
     * Default implementation that does full masking
     */
    default String defaultMask(String value, char maskChar) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return String.valueOf(maskChar).repeat(value.length());
    }
}
