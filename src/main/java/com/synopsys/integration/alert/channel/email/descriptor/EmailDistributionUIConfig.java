/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ProviderUserModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;

@Component
public class EmailDistributionUIConfig extends ChannelDistributionUIConfig {
    private static final String LABEL_SUBJECT_LINE = "Subject Line";
    private static final String LABEL_ADDITIONAL_ADDRESSES = "Additional Email Addresses";
    private static final String LABEL_PROJECT_OWNER_ONLY = "Project Owner Only";

    private static final String EMAIL_SUBJECT_LINE_DESCRIPTION = "The subject line to use in the emails sent for this distribution job.";
    private static final String DESCRIPTION_ADDITIONAL_ADDRESSES = "Any additional email addresses (for valid users of the provider) that notifications from this job should be sent to.";
    private static final String EMAIL_PROJECT_OWNER_ONLY_DESCRIPTION = "If true, emails will only be sent to the administrator(s) of the project. Otherwise, all users assigned to the project will get an email.";

    private ProviderDataAccessor providerDataAccessor;

    @Autowired
    public EmailDistributionUIConfig(@Lazy final DescriptorMap descriptorMap, ProviderDataAccessor providerDataAccessor) {
        super(EmailChannel.COMPONENT_NAME, EmailDescriptor.EMAIL_LABEL, EmailDescriptor.EMAIL_URL, EmailDescriptor.EMAIL_ICON, descriptorMap);
        this.providerDataAccessor = providerDataAccessor;
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        // FIXME this is provider specific
        //  in the future we could have a dynamically populated select field
        final Set<LabelValueSelectOption> emailAddresses = providerDataAccessor.getAllUsers(BlackDuckProvider.COMPONENT_NAME)
                                                               .stream()
                                                               .map(ProviderUserModel::getEmailAddress)
                                                               .sorted()
                                                               .map(LabelValueSelectOption::new)
                                                               .collect(Collectors.toCollection(LinkedHashSet::new));

        final ConfigField subjectLine = TextInputConfigField.create(EmailDescriptor.KEY_SUBJECT_LINE, LABEL_SUBJECT_LINE, EMAIL_SUBJECT_LINE_DESCRIPTION);
        final ConfigField additionalEmailAddresses = SelectConfigField.create(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, LABEL_ADDITIONAL_ADDRESSES, DESCRIPTION_ADDITIONAL_ADDRESSES, emailAddresses, true);
        final ConfigField projectOwnerOnly = CheckboxConfigField.create(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, LABEL_PROJECT_OWNER_ONLY, EMAIL_PROJECT_OWNER_ONLY_DESCRIPTION);
        return List.of(subjectLine, additionalEmailAddresses, projectOwnerOnly);
    }

}
