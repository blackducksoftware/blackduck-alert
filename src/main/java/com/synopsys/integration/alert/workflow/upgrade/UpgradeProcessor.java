/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.workflow.upgrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UpgradeProcessor {
    private final Logger logger = LoggerFactory.getLogger(UpgradeProcessor.class);

    private final AlertVersionUtil alertVersionUtil;
    private final DescriptorRegistrator descriptorRegistrator;

    @Autowired
    public UpgradeProcessor(final AlertVersionUtil alertVersionUtil, final DescriptorRegistrator descriptorRegistrator) {
        this.alertVersionUtil = alertVersionUtil;
        this.descriptorRegistrator = descriptorRegistrator;
    }

    public void runUpgrade() {
        logger.info("Upgrading alert...");
        descriptorRegistrator.registerDescriptors();
        alertVersionUtil.updateVersionInDB(alertVersionUtil.findFileVersion());
    }

    public boolean shouldUpgrade() {
        final AlertVersion alertVersion = alertVersionUtil.findAlertVersion();
        final String buildVersion = alertVersion.getFileVersion();
        final String serverVersion = alertVersion.getDbVersion();
        logger.info("Alert build version is {} and the server version is {}.", buildVersion, serverVersion);
        return !alertVersionUtil.doVersionsMatch(serverVersion, buildVersion);
    }
}
