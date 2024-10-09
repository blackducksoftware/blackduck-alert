package com.blackduck.integration.alert.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.blackduck.integration.blackduck.http.transform.BlackDuckJsonTransformer;
import com.blackduck.integration.blackduck.http.transform.subclass.BlackDuckResponseResolver;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.log.Slf4jIntLogger;
import com.blackduck.integration.rest.support.AuthenticationSupport;
import com.google.gson.Gson;

@Configuration
public class BlackDuckConfiguration {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckConfiguration.class);

    @Bean
    public Gson gson() {
        return BlackDuckServicesFactory.createDefaultGson();
    }

    @Bean
    public BlackDuckResponseResolver blackDuckResponseResolver() {
        return new BlackDuckResponseResolver(gson());
    }

    @Bean
    public BlackDuckJsonTransformer blackDuckJsonTransformer() {
        return new BlackDuckJsonTransformer(gson(), BlackDuckServicesFactory.createDefaultObjectMapper(), blackDuckResponseResolver(), new Slf4jIntLogger(logger));
    }

    @Bean
    public AuthenticationSupport authenticationSupport() {
        return new AuthenticationSupport();
    }

}
