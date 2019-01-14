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
package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertUpgradeException;
import com.synopsys.integration.alert.workflow.upgrade.step.UpgradeStep;

@Component
public class UpgradeStep_4_0_0 extends UpgradeStep {
    private final DescriptorRegistrar descriptorRegistrar;

    @Autowired
    public UpgradeStep_4_0_0(final DescriptorRegistrar descriptorRegistrar) {
        super("4.0.0");
        this.descriptorRegistrar = descriptorRegistrar;
    }

    @Override
    public void runUpgrade() throws AlertUpgradeException {
        try {
            descriptorRegistrar.registerDescriptors();
        } catch (final AlertDatabaseConstraintException e) {
            throw new AlertUpgradeException("Error when registering descriptors and fields", e);
        }
    }

    // TODO Move all of our data from our old config tables to our new ones here.
}
