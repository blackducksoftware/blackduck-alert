package com.blackducksoftware.integration.hub.notification;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Application {

    private final Logger logger = LoggerFactory.getLogger(Application.class);

    @PostConstruct
    void init() {
        logger.debug("STARTED THE NOTIFICATION ENGINE");
    }

    public static void main(final String[] args) {
        new SpringApplicationBuilder(Application.class).logStartupInfo(false).run(args);
    }
}
