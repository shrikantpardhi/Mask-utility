package com.utility.masking.slf4j;

import ch.qos.logback.classic.pattern.MessageConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.utility.masking.annotation.Sensitive;
import com.utility.masking.service.EnvironmentChecker;
import com.utility.masking.service.MaskingService;

import java.lang.reflect.Field;

/**
 * Custom SLF4J message converter that automatically masks sensitive data
 * 
 * Configuration in logback.xml:
 * <pre>
 * &lt;conversionRule conversionWord="maskedMsg" 
 *                 converterClass="com.utility.masking.slf4j.SensitiveDataMessageConverter" /&gt;
 * 
 * &lt;pattern&gt;%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %maskedMsg%n&lt;/pattern&gt;
 * </pre>
 */
public class SensitiveDataMessageConverter extends MessageConverter {

    private static MaskingService maskingService;
    private static EnvironmentChecker environmentChecker;

    public static void setMaskingService(MaskingService service) {
        maskingService = service;
    }

    public static void setEnvironmentChecker(EnvironmentChecker checker) {
        environmentChecker = checker;
    }

    @Override
    public String convert(ILoggingEvent event) {
        // If masking is disabled, use default behavior
        if (maskingService == null || environmentChecker == null || !environmentChecker.shouldMask()) {
            return super.convert(event);
        }

        // Get the formatted message
        String formattedMessage = event.getFormattedMessage();
        Object[] argumentArray = event.getArgumentArray();

        if (argumentArray == null || argumentArray.length == 0) {
            return formattedMessage;
        }

        // Replace sensitive arguments with masked versions
        String result = formattedMessage;
        for (Object arg : argumentArray) {
            if (arg != null && hasSensitiveFields(arg.getClass())) {
                String original = arg.toString();
                String masked = maskingService.maskData(arg);
                result = result.replace(original, masked);
            }
        }

        return result;
    }

    private boolean hasSensitiveFields(Class<?> clazz) {
        if (clazz == null || clazz == Object.class) {
            return false;
        }

        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(Sensitive.class)) {
                return true;
            }
        }

        return hasSensitiveFields(clazz.getSuperclass());
    }
}
