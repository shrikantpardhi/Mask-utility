package com.utility.masking.config;

import com.utility.masking.service.MaskingService;
import com.utility.masking.converter.MaskingMessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration for data masking functionality
 */
@AutoConfiguration
@ConditionalOnClass(MaskingService.class)
public class MaskingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public MaskingService maskingService() {
        return new MaskingService();
    }

    @Bean
    @ConditionalOnMissingBean
    public MaskingMessageConverter maskingMessageConverter(MaskingService maskingService) {
        return new MaskingMessageConverter(maskingService);
    }
}
