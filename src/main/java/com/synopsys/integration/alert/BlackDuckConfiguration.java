/*
 * blackduck-alert
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlackDuckConfiguration {
    private final Logger logger = LoggerFactory.getLogger(BlackDuckConfiguration.class);

    //    @Bean
    //    public Gson gson() {
    //        return BlackDuckServicesFactory.createDefaultGson();
    //    }
    //
    //    @Bean
    //    public ObjectMapper objectMapper() {
    //        return BlackDuckServicesFactory.createDefaultObjectMapper();
    //    }
    //
    //    @Bean
    //    public BlackDuckResponseResolver blackDuckResponseResolver() {
    //        return new BlackDuckResponseResolver(gson());
    //    }
    //
    //    @Bean
    //    public BlackDuckJsonTransformer blackDuckJsonTransformer() {
    //        return new BlackDuckJsonTransformer(gson(), objectMapper(), blackDuckResponseResolver(), new Slf4jIntLogger(logger));
    //    }
    //
    //    @Bean
    //    public AuthenticationSupport authenticationSupport() {
    //        return new AuthenticationSupport();
    //    }
    //
}
