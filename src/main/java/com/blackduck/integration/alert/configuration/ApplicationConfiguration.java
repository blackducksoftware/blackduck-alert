/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.configuration;

import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.filter.ForwardedHeaderFilter;

import com.blackduck.integration.alert.common.AlertProperties;
import com.blackduck.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.blackduck.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.blackduck.integration.alert.common.security.EncryptionUtility;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.google.gson.Gson;

@Configuration
@AutoConfigureOrder(1)
public class ApplicationConfiguration {
    private final Gson gson;

    @Autowired
    public ApplicationConfiguration(Gson gson) {
        this.gson = gson;
    }

    @Bean
    public AlertProperties alertProperties() {
        return new AlertProperties();
    }

    @Bean
    public FilePersistenceUtil filePersistenceUtil() {
        return new FilePersistenceUtil(alertProperties(), gson);
    }

    @Bean
    public EncryptionUtility encryptionUtility() {
        return new EncryptionUtility(alertProperties(), filePersistenceUtil());
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new ResourcelessTransactionManager();
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        return threadPoolTaskScheduler;
    }

    @Bean
    public HttpSessionCsrfTokenRepository csrfTokenRepository() {
        return new HttpSessionCsrfTokenRepository();
    }

    @Bean
    public PasswordEncoder defaultPasswordEncoder() {
        return new BCryptPasswordEncoder(16);
    }

    @Bean
    public AuthorizationManager authorizationManager(RoleAccessor roleAccessor) {
        return new AuthorizationManager(roleAccessor);
    }

    @Bean
    ForwardedHeaderFilter forwardedHeaderFilter() {
        return new ForwardedHeaderFilter();
    }
}
