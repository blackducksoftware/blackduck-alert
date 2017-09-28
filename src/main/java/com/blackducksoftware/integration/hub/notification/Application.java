package com.blackducksoftware.integration.hub.notification;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.api.nonpublic.HubVersionRequestService;

@SpringBootApplication
public class Application {

    private final Logger logger = LoggerFactory.getLogger(Application.class);

    @Autowired
    private EngineProperties engineProperties;

    @Autowired
    private HubServiceWrapper hubServiceWrapper;

    @PostConstruct
    void init() {
        logger.info("Notification Engine Starting...");
        logger.info("Engine Configuration: ");
        logger.info("Hub URL:            {}", engineProperties.getHubUrl());
        logger.info("Hub Username:       {}", engineProperties.getHubUsername());
        logger.info("Hub Password:       **********");
        logger.info("Hub Timeout:        {}", engineProperties.getHubTimeout());
        logger.info("Hub Proxy Host:     {}", engineProperties.getHubProxyHost());
        logger.info("Hub Proxy Port:     {}", engineProperties.getHubProxyPort());
        logger.info("Hub Proxy User:     {}", engineProperties.getHubProxyUsername());
        logger.info("Hub Proxy Password: **********", engineProperties.getHubUrl());

        try {
            hubServiceWrapper.init();
            final HubVersionRequestService versionRequestService = hubServiceWrapper.getHubServicesFactory().createHubVersionRequestService();
            final String hubVersion = versionRequestService.getHubVersion();
            logger.info("Hub Version: {}", hubVersion);
        } catch (final IntegrationException ex) {
            logger.error("Error occurred initializing the notfication engine", ex);
        }
    }

    public static void main(final String[] args) {
        new SpringApplicationBuilder(Application.class).logStartupInfo(false).run(args);
    }
}
