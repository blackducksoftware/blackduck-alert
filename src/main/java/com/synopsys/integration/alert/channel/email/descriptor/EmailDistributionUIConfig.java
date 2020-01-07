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

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannelKey;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.EndpointTableSelectField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.TableSelectColumn;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class EmailDistributionUIConfig extends ChannelDistributionUIConfig {
    private static final String LABEL_SUBJECT_LINE = "Subject Line";
    private static final String LABEL_ADDITIONAL_ADDRESSES = "Additional Email Addresses";
    private static final String LABEL_ADDITIONAL_ADDRESSES_ONLY = "Additional Email Addresses Only";
    private static final String LABEL_PROJECT_OWNER_ONLY = "Project Owner Only";

    private static final String EMAIL_SUBJECT_LINE_DESCRIPTION = "The subject line to use in the emails sent for this distribution job.";
    private static final String DESCRIPTION_ADDITIONAL_ADDRESSES = "Any additional email addresses (for valid users of the provider) that notifications from this job should be sent to.";
    private static final String DESCRIPTION_ADDITIONAL_ADDRESSES_ONLY = "Rather than sending emails to users assigned to the configured projects, send emails to only the users selected in 'Additional Email Addresses'.";
    private static final String EMAIL_PROJECT_OWNER_ONLY_DESCRIPTION = "If true, emails will only be sent to the administrator(s) of the project. Otherwise, all users assigned to the project will get an email.";

    @Autowired
    public EmailDistributionUIConfig(EmailChannelKey emailChannelKey) {
        super(emailChannelKey, EmailDescriptor.EMAIL_LABEL, EmailDescriptor.EMAIL_URL);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        ConfigField subjectLine = TextInputConfigField.create(EmailDescriptor.KEY_SUBJECT_LINE, LABEL_SUBJECT_LINE, EMAIL_SUBJECT_LINE_DESCRIPTION);
        ConfigField additionalEmailAddresses = EndpointTableSelectField.createSearchable(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, LABEL_ADDITIONAL_ADDRESSES, DESCRIPTION_ADDITIONAL_ADDRESSES)
                                                   .addColumn(new TableSelectColumn("emailAddress", "Email Address", true, true))
                                                   .addRequestedDataFieldKey(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
        ConfigField additionalEmailAddressesOnly = CheckboxConfigField
                                                       .create(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY, LABEL_ADDITIONAL_ADDRESSES_ONLY, DESCRIPTION_ADDITIONAL_ADDRESSES_ONLY, this::validateAdditionalEmailAddressesOnly);
        ConfigField projectOwnerOnly = CheckboxConfigField.create(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, LABEL_PROJECT_OWNER_ONLY, EMAIL_PROJECT_OWNER_ONLY_DESCRIPTION);
        additionalEmailAddressesOnly.disallowField(projectOwnerOnly.getKey());

        return List.of(subjectLine, additionalEmailAddresses, additionalEmailAddressesOnly, projectOwnerOnly);
    }

    private Collection<String> validateAdditionalEmailAddressesOnly(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        Boolean useOnlyAdditionalEmailAddresses = fieldToValidate.getValue().map(Boolean::parseBoolean).orElse(false);
        if (useOnlyAdditionalEmailAddresses) {
            boolean hasAdditionalAddresses = fieldModel
                                                 .getFieldValueModel(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES)
                                                 .map(FieldValueModel::getValues)
                                                 .filter(additionalEmailAddresses -> !additionalEmailAddresses.isEmpty())
                                                 .isPresent();
            if (!hasAdditionalAddresses) {
                return Set.of("No additional email addresses were provided.");
            }
        }
        return Set.of();
    }

}
