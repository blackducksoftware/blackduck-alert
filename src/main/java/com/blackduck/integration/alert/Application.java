/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.batch.autoconfigure.BatchAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.blackduck.integration.alert.component.authentication.security.database.UserDatabaseService;

@EnableJpaRepositories(basePackages = {"com.blackduck.integration.alert.database", "com.blackduck.integration.alert.channel", "com.blackduck.integration.alert.api.oauth",
        "com.blackduck.integration.alert.authentication"})
@EnableTransactionManagement
@EnableBatchProcessing
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@EnableMethodSecurity(prePostEnabled = true)
@SpringBootApplication(exclude = {BatchAutoConfiguration.class})
public class Application {

    @Autowired
    private UserDatabaseService userDatabaseService;

    public static void main(String[] args) {
        new SpringApplicationBuilder(Application.class)
                .web(WebApplicationType.SERVLET)
                .logStartupInfo(false)
                .run(args);
    }

    @Bean
    public DaoAuthenticationProvider alertDatabaseAuthProvider(PasswordEncoder defaultPasswordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDatabaseService);
        provider.setPasswordEncoder(defaultPasswordEncoder);
        return provider;
    }
}
