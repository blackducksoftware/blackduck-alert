/*
 * workflow
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
package com.synopsys.integration.alert.workflow.startup;

import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.persistence.accessor.SystemStatusAccessor;
import com.synopsys.integration.alert.workflow.startup.component.StartupComponent;

@Configuration
public class StartupManager {
    private final Logger logger = LoggerFactory.getLogger(StartupManager.class);

    private final SystemStatusAccessor systemStatusAccessor;
    private final List<StartupComponent> startupComponents;

    @Autowired
    public StartupManager(SystemStatusAccessor systemStatusAccessor, List<StartupComponent> startupComponents) {
        this.systemStatusAccessor = systemStatusAccessor;
        this.startupComponents = startupComponents;
    }

    @PostConstruct
    @Transactional
    public void init() {
        startup();
    }

    public void startup() {
        logger.info("Alert Starting...");
        systemStatusAccessor.startupOccurred();
        startupComponents.forEach(StartupComponent::initializeComponent);
    }

}
