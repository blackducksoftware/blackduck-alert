/*
 * blackduck-alert
 *
 * Copyright (c) 2021 Synopsys, Inc.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.transaction.PlatformTransactionManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.accessor.RoleAccessor;
import com.synopsys.integration.alert.common.persistence.util.FilePersistenceUtil;
import com.synopsys.integration.alert.common.security.EncryptionUtility;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.rest.RestConstants;
import com.synopsys.integration.rest.support.AuthenticationSupport;

@Configuration
@AutoConfigureOrder(1)
public class ApplicationConfiguration {
    private final Logger logger = LoggerFactory.getLogger(ApplicationConfiguration.class);

    @Bean
    public AlertProperties alertProperties() {
        return new AlertProperties();
    }

    @Bean
    public FilePersistenceUtil filePersistenceUtil() {
        return new FilePersistenceUtil(alertProperties(), gson());
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
    public MapJobRepositoryFactoryBean mapJobRepositoryFactory() throws Exception {
        MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean(transactionManager());
        factory.afterPropertiesSet();

        return factory;
    }

    @Bean
    public JobRepository jobRepository() throws Exception {
        return mapJobRepositoryFactory().getObject();
    }

    @Bean
    public SimpleJobLauncher jobLauncher() {
        SimpleJobLauncher launcher = new SimpleJobLauncher();
        try {
            launcher.setJobRepository(jobRepository());
        } catch (Exception ex) {
            logger.error("Creating job launcher bean", ex);
        }
        return launcher;
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(Runtime.getRuntime().availableProcessors());
        return threadPoolTaskScheduler;
    }

    @Bean
    public TaskExecutor taskExecutor() {
        return new SyncTaskExecutor();
    }

    @Bean
    public Gson gson() {
        return new GsonBuilder().setDateFormat(RestConstants.JSON_DATE_FORMAT).create();
    }

    @Bean
    public AuthenticationSupport authenticationSupport() {
        return new AuthenticationSupport();
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
    public DefaultJmsListenerContainerFactory defaultJmsListenerContainerFactory(CachingConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        return factory;
    }

    @Bean
    public DefaultJmsListenerContainerFactory distributionChannelJmsListenerContainerFactory(CachingConnectionFactory connectionFactory) {
        DefaultJmsListenerContainerFactory factory = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        int minConcurrency = 1;
        int maxThreadCount = Runtime.getRuntime().availableProcessors();
        int maxConcurrency = maxThreadCount < 10 ? maxThreadCount : 10;
        String concurrencyRange = String.format("%s-%s", minConcurrency, maxConcurrency);
        factory.setConcurrency(concurrencyRange);

        return factory;
    }
}
