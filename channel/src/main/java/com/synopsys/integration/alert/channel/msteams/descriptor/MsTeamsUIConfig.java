/**
 * channel
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.msteams.descriptor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.msteams.MsTeamsKey;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.URLInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;

@Component
public class MsTeamsUIConfig extends ChannelDistributionUIConfig {
    private static final String LABEL_WEBHOOK = "Webhook";

    private static final String MSTEAMS_WEBHOOK_DESCRIPTION = "The MS Teams URL to receive alerts.";

    @Autowired
    public MsTeamsUIConfig(MsTeamsKey msTeamsKey) {
        super(msTeamsKey, MsTeamsDescriptor.MSTEAMS_LABEL, MsTeamsDescriptor.MSTEAMS_URL);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        ConfigField webhook = new URLInputConfigField(MsTeamsDescriptor.KEY_WEBHOOK, LABEL_WEBHOOK, MSTEAMS_WEBHOOK_DESCRIPTION).applyRequired(true);
        return List.of(webhook);
    }

}
