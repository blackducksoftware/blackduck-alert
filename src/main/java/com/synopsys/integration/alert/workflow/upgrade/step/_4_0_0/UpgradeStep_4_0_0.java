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

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertUpgradeException;
import com.synopsys.integration.alert.workflow.upgrade.step.UpgradeStep;
import com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion.DataUpgrade;

@Component
public class UpgradeStep_4_0_0 extends UpgradeStep {
    private final Logger logger = LoggerFactory.getLogger(UpgradeStep_4_0_0.class);
    private final List<DataUpgrade> dataTransfers;

    @Autowired
    public UpgradeStep_4_0_0(final List<DataUpgrade> dataTransfers) {
        super("4.0.0");
        this.dataTransfers = dataTransfers;
    }

    @Override
    public void runUpgrade() throws AlertUpgradeException {
        logger.warn("Email global configuration environment variable changed from 'ALERT_CHANNEL_EMAIL_MAIL_SMTP_DNS_RET' to 'ALERT_CHANNEL_EMAIL_MAIL_SMTP_DSN_RET'. Please change this variable if in use.");
        for (final DataUpgrade dataUpgrade : dataTransfers) {
            try {
                dataUpgrade.upgrade();
            } catch (final AlertDatabaseConstraintException e) {
                throw new AlertUpgradeException("Error moving data for descriptor " + dataUpgrade.getDescriptorName());
            }
        }
    }

}
