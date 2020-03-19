/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.email.descriptor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;

@Component
public class EmailDistributionUIConfig extends ChannelDistributionUIConfig {
    private static final String LABEL_SUBJECT_LINE = "Subject Line";
    private static final String LABEL_PROJECT_OWNER_ONLY = "Project Owner Only";

    private static final String EMAIL_SUBJECT_LINE_DESCRIPTION = "The subject line to use in the emails sent for this distribution job.";
    private static final String EMAIL_PROJECT_OWNER_ONLY_DESCRIPTION = "If true, emails will only be sent to the administrator(s) of the project. Otherwise, all users assigned to the project will get an email.";

    @Autowired
    public EmailDistributionUIConfig(@Lazy final DescriptorMap descriptorMap) {
        super(EmailChannel.COMPONENT_NAME, EmailDescriptor.EMAIL_LABEL, EmailDescriptor.EMAIL_URL, EmailDescriptor.EMAIL_ICON, descriptorMap);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        final ConfigField subjectLine = TextInputConfigField.create(EmailDescriptor.KEY_SUBJECT_LINE, LABEL_SUBJECT_LINE, EMAIL_SUBJECT_LINE_DESCRIPTION);
        final ConfigField projectOwnerOnly = CheckboxConfigField.create(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, LABEL_PROJECT_OWNER_ONLY, EMAIL_PROJECT_OWNER_ONLY_DESCRIPTION);
        return List.of(subjectLine, projectOwnerOnly);
    }
}
