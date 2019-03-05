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
package com.synopsys.integration.alert.channel.email.descriptor;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;

@Component
public class EmailDistributionUIConfig extends ChannelDistributionUIConfig {

    @Autowired
    public EmailDistributionUIConfig(final DescriptorAccessor descriptorAccessor) {
        super(EmailDescriptor.EMAIL_LABEL, EmailDescriptor.EMAIL_URL, EmailDescriptor.EMAIL_ICON, descriptorAccessor);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        final ConfigField emailAddresses = TextInputConfigField.create(EmailDescriptor.KEY_EMAIL_ADDRESSES, "Email Addresses");
        final ConfigField subjectLine = TextInputConfigField.create(EmailDescriptor.KEY_SUBJECT_LINE, "Subject Line");
        final ConfigField projectOwnerOnly = CheckboxConfigField.create(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, "Project Owner Only");
        return Arrays.asList(emailAddresses, subjectLine, projectOwnerOnly);
    }
}
