/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.synopsys.integration.alert.web.security.authentication.database.UserDatabaseService;
import com.synopsys.integration.alert.workflow.startup.StartupManager;

@EnableJpaRepositories(basePackages = { "com.synopsys.integration.alert.database" })
@EnableTransactionManagement
@EnableBatchProcessing
@EnableScheduling
@EnableAsync(proxyTargetClass = true)
@EnableJms
@EnableGlobalMethodSecurity(prePostEnabled = true)
@SpringBootApplication(exclude = { BatchAutoConfiguration.class })
@AutoConfigureOrder(4)
public class Application {
    private static final Logger logger = LoggerFactory.getLogger(Application.class);
    @Autowired
    private UserDatabaseService userDatabaseService;
    @Autowired
    private StartupManager startupManager;

    public static void main(final String[] args) {
        new SpringApplicationBuilder(Application.class).logStartupInfo(false).run(args);
    }

    @PostConstruct
    void init() {
        startupManager.startup();
    }

    @Bean
    public DaoAuthenticationProvider alertDatabaseAuthProvider(final PasswordEncoder defaultPasswordEncoder) {
        final DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDatabaseService);
        provider.setPasswordEncoder(defaultPasswordEncoder);
        return provider;
    }
}
