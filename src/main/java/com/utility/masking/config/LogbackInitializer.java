package com.utility.masking.config;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

/**
 * Initializes Logback configuration from the JAR if not already configured
 * This ensures the masking converter is always available
 */
public class LogbackInitializer implements ApplicationListener<ApplicationEnvironmentPreparedEvent>, Ordered {

    private static final String LOGBACK_CONFIG_FILE = "logback-spring.xml";
    
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        // Check if user has provided their own logback configuration
        String logbackConfig = System.getProperty("logback.configurationFile");
        
        if (logbackConfig == null || logbackConfig.isEmpty()) {
            // User hasn't provided custom logback config, check if our default exists
            ClassPathResource resource = new ClassPathResource(LOGBACK_CONFIG_FILE);
            
            if (resource.exists()) {
                try {
                    configureLogback(resource);
                } catch (Exception e) {
                    // Silently fail - Spring Boot will use its defaults
                    System.err.println("Warning: Could not configure logback from JAR: " + e.getMessage());
                }
            }
        }
    }

    private void configureLogback(ClassPathResource resource) throws IOException, JoranException {
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        JoranConfigurator configurator = new JoranConfigurator();
        configurator.setContext(context);
        
        try (InputStream inputStream = resource.getInputStream()) {
            context.reset();
            configurator.doConfigure(inputStream);
        }
    }

    @Override
    public int getOrder() {
        // Run early, before logging is fully initialized
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
}
