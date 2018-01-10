/**
 * hub-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.alert.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;

@Configuration
public class DataSourceConfig {

    @Value("${blackduck.alertdb.url:jdbc:h2:file:./data/alertdb}")
    String alertDbUrl;

    @Value("${blackduck.alertdb.username:sa}")
    String alertDbUsername;

    @Value("${blackduck.alertdb.password:}")
    String alertDbPassword;

    @Value("${blackduck.alertdb.driver-class-name:org.h2.Driver}")
    String alertDbDriverClassName;

    // USING SPRING BATCH HERE. For JPA to work need to configure the JPATransactionManager bean.
    // SEE SPRING ISSUE: https://jira.spring.io/browse/BATCH-2642
    // Stack Overflow: https://stackoverflow.com/questions/38287298/persist-issue-with-a-spring-batch-itemwriter-using-a-jpa-repository
    @Bean
    public DataSource dataSource() {
        return DataSourceBuilder.create().url(alertDbUrl).username(alertDbUsername).password(alertDbPassword).driverClassName(alertDbDriverClassName).build();
    }

    @Bean
    @Primary
    public JpaTransactionManager jpaTransactionManager(final DataSource dataSource) {
        final JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setDataSource(dataSource);
        return transactionManager;
    }
}
