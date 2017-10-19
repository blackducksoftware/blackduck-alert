/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import com.blackducksoftware.integration.hub.alert.channel.AbstractJmsTemplate;

@Configuration
public class BeanProvider {
    private final static Logger logger = LoggerFactory.getLogger(BeanProvider.class);

    private final AlertProperties alertProperties;
    // private final PlatformTransactionManager txManager;
    // private final MapJobRepositoryFactoryBean factory;
    // private final JobRepository jobRepository;
    private final List<AbstractJmsTemplate> templateList;

    @Autowired
    public BeanProvider(final AlertProperties alertProperties, final List<AbstractJmsTemplate> templateList) {
        // public BeanProvider(final AlertProperties alertProperties, final PlatformTransactionManager txManager, final MapJobRepositoryFactoryBean factory, final JobRepository jobRepository, final List<AbstractJmsTemplate> templateList) {
        this.alertProperties = alertProperties;
        // this.txManager = txManager;
        // this.factory = factory;
        // this.jobRepository = jobRepository;
        this.templateList = templateList;
    }

    // @Bean
    // public String accumulatorCronExpression() {
    // return alertProperties.getAccumulatorCron();
    // }
    //
    // @Bean
    // public PlatformTransactionManager transactionManager() {
    // return new ResourcelessTransactionManager();
    // }
    //
    // @Bean
    // public MapJobRepositoryFactoryBean mapJobRepositoryFactory() throws Exception {
    // final MapJobRepositoryFactoryBean factory = new MapJobRepositoryFactoryBean(transactionManager());
    // factory.afterPropertiesSet();
    // return factory;
    // }
    //
    // @Bean
    // public JobRepository jobRepository() throws Exception {
    // return mapJobRepositoryFactory().getObject();
    // }
    //
    // @Bean
    // public SimpleJobLauncher jobLauncher() throws Exception {
    // final SimpleJobLauncher launcher = new SimpleJobLauncher();
    // launcher.setJobRepository(jobRepository());
    // return launcher;
    // }
    //
    // @Bean
    // public TaskExecutor taskExecutor() {
    // final TaskExecutor executor = new SyncTaskExecutor();
    // return executor;
    // }
    //
    // @Bean
    // public HubServiceWrapper hubServiceWrapper() {
    // final HubServiceWrapper wrapper = new HubServiceWrapper(alertProperties);
    // try {
    // wrapper.init();
    // } catch (final AlertException ex) {
    // logger.error("Error initializing the service wrapper", ex);
    // }
    // return wrapper;
    // }
    //
    // @Bean
    // public Gson gson() {
    // return new GsonBuilder().setDateFormat(RestConnection.JSON_DATE_FORMAT).create();
    // }
    //
    // @Bean
    // public ChannelTemplateManager channelTemplateManager() {
    // final ChannelTemplateManager channelTemplateManager = new ChannelTemplateManager();
    // templateList.forEach(jmsTemplate -> {
    // channelTemplateManager.addTemplate(jmsTemplate.getDestinationName(), jmsTemplate);
    // });
    // return channelTemplateManager;
    // }
}
