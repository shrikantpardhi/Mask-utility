package com.utility.masking.strategy;

/**
 * Enum defining different masking strategies for sensitive data
 */
public enum MaskStrategy {
    /**
     * Masks all characters with asterisks
     * Example: "password123" -> "***********"
     */
    FULL,

    /**
     * Keeps first and last character, masks everything in between
     * Example: "password123" -> "p*********3"
     */
    FIRST_LAST,

    /**
     * Keeps last 4 characters, masks everything before
     * Example: "1234567890" -> "******7890"
     */
    LAST_FOUR,

    /**
     * Custom masking logic (can be extended)
     */
    CUSTOM
}
