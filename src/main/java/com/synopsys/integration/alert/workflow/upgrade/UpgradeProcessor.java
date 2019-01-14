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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertUpgradeException;
import com.synopsys.integration.alert.workflow.upgrade.step.UpgradeStep;
import com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.DescriptorRegistrar;

@Component
public class UpgradeProcessor {
    private final Logger logger = LoggerFactory.getLogger(UpgradeProcessor.class);

    private final AlertVersionUtil alertVersionUtil;
    private final List<UpgradeStep> upgradeSteps;
    private DescriptorRegistrar descriptorRegistrar;

    @Autowired
    public UpgradeProcessor(final AlertVersionUtil alertVersionUtil, DescriptorRegistrar descriptorRegistrar, final List<UpgradeStep> upgradeSteps) {
        this.alertVersionUtil = alertVersionUtil;
        this.descriptorRegistrar = descriptorRegistrar;
        this.upgradeSteps = upgradeSteps;
    }

    public void runUpgrade() throws AlertUpgradeException {
        logger.info("Upgrading alert...");
        try {
            descriptorRegistrar.registerDescriptors();
        } catch (final AlertDatabaseConstraintException e) {
            throw new AlertUpgradeException("Error when registering descriptors and fields", e);
        }
        final Map<SemanticVersion, UpgradeStep> upgradeProcessSteps = initializeUpgradeMap();
        final List<SemanticVersion> sortedVersions = upgradeProcessSteps.keySet()
                                                         .stream()
                                                         .sorted()
                                                         .collect(Collectors.toList());
        for (final SemanticVersion semanticVersion : sortedVersions) {
            final UpgradeStep upgradeStep = upgradeProcessSteps.get(semanticVersion);
            final String version = semanticVersion.getVersionString();
            logger.info("Running upgrade for {}", version);
            upgradeStep.runUpgrade();
            alertVersionUtil.updateVersionInDB(version);
        }
    }

    public boolean shouldUpgrade() {
        final String buildVersion = alertVersionUtil.findFileVersion();
        final String serverVersion = alertVersionUtil.findDBVersion();
        logger.info("Alert build version is {} and the server version is {}.", buildVersion, serverVersion);
        return !buildVersion.equals(serverVersion);
    }

    private Map<SemanticVersion, UpgradeStep> initializeUpgradeMap() {
        final SemanticVersion buildVersion = new SemanticVersion(alertVersionUtil.findFileVersion());
        final SemanticVersion serverVersion = new SemanticVersion(alertVersionUtil.findDBVersion());
        final Map<SemanticVersion, UpgradeStep> stepMap = new HashMap<>();
        for (final UpgradeStep upgradeStep : upgradeSteps) {
            final SemanticVersion stepVersion = new SemanticVersion(upgradeStep.getVersion());
            if (stepVersion.isGreaterThan(serverVersion) && stepVersion.isLessThanOrEqual(buildVersion)) {
                stepMap.put(new SemanticVersion(upgradeStep.getVersion()), upgradeStep);
            }
        }
        return stepMap;
    }
}
