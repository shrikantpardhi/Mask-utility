package com.utility.masking.annotation;

import com.utility.masking.strategy.MaskStrategy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark fields as sensitive for automatic masking in logs
 * 
 * When any object containing @Sensitive fields is logged, those fields
 * will be automatically masked according to the specified strategy.
 * 
 * Usage:
 * <pre>
 * public class User {
 *     @Sensitive(strategy = MaskStrategy.FULL)
 *     private String password;
 *     
 *     @Sensitive(strategy = MaskStrategy.LAST_FOUR)
 *     private String creditCard;
 * }
 * 
 * // When logged:
 * log.info("User: {}", user); // Sensitive fields automatically masked
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Sensitive {
    
    /**
     * The masking strategy to apply
     * @return the mask strategy, defaults to FULL
     */
    MaskStrategy strategy() default MaskStrategy.FULL;
    
    /**
     * Custom mask character (default is asterisk)
     * @return the character to use for masking
     */
    char maskChar() default '*';
    
    /**
     * Custom masking strategy class (for CUSTOM strategy)
     * @return the custom masker class
     */
    Class<? extends com.utility.masking.masker.DataMasker> customMasker() 
        default com.utility.masking.masker.DataMasker.class;
}
