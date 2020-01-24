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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailChannelKey;
import com.synopsys.integration.alert.channel.email.template.EmailAttachmentFormat;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
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
    private static final String LABEL_ATTACHMENT_FORMAT = "Attachment File Type";

    private static final String DESCRIPTION_EMAIL_SUBJECT_LINE = "The subject line to use in the emails sent for this distribution job.";
    private static final String DESCRIPTION_ADDITIONAL_ADDRESSES = "Any additional email addresses (for valid users of the provider) that notifications from this job should be sent to.";
    private static final String DESCRIPTION_ADDITIONAL_ADDRESSES_ONLY = "Rather than sending emails to users assigned to the configured projects, send emails to only the users selected in 'Additional Email Addresses'.";
    private static final String DESCRIPTION_EMAIL_PROJECT_OWNER_ONLY = "If true, emails will only be sent to the administrator(s) of the project. Otherwise, all users assigned to the project will get an email.";
    private static final String DESCRIPTION_ATTACHMENT_FORMAT = "If a file type is selected, a file of that type, representing the message content, will be attached to the email.";

    @Autowired
    public EmailDistributionUIConfig(EmailChannelKey emailChannelKey) {
        super(emailChannelKey, EmailDescriptor.EMAIL_LABEL, EmailDescriptor.EMAIL_URL);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        ConfigField subjectLine = new TextInputConfigField(EmailDescriptor.KEY_SUBJECT_LINE, LABEL_SUBJECT_LINE, DESCRIPTION_EMAIL_SUBJECT_LINE);
        ConfigField additionalEmailAddresses = new EndpointTableSelectField(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, LABEL_ADDITIONAL_ADDRESSES, DESCRIPTION_ADDITIONAL_ADDRESSES)
                                                   .applyColumn(new TableSelectColumn("emailAddress", "Email Address", true, true))
                                                   .applySearchable(true)
                                                   .applyRequestedDataFieldKey(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);
        ConfigField additionalEmailAddressesOnly = new CheckboxConfigField(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY, LABEL_ADDITIONAL_ADDRESSES_ONLY, DESCRIPTION_ADDITIONAL_ADDRESSES_ONLY)
                                                       .applyValidationFunctions(this::validateAdditionalEmailAddressesOnly)
                                                       .applyDisallowedRelatedField(EmailDescriptor.KEY_PROJECT_OWNER_ONLY);
        ConfigField projectOwnerOnly = new CheckboxConfigField(EmailDescriptor.KEY_PROJECT_OWNER_ONLY, LABEL_PROJECT_OWNER_ONLY, DESCRIPTION_EMAIL_PROJECT_OWNER_ONLY)
                                           .applyDisallowedRelatedField(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY);

        List<LabelValueSelectOption> attachmentFormats = Stream
                                                             .of(EmailAttachmentFormat.values())
                                                             .map(EmailAttachmentFormat::name)
                                                             .map(LabelValueSelectOption::new)
                                                             .collect(Collectors.toList());
        ConfigField attachmentFormat = new SelectConfigField(EmailDescriptor.KEY_EMAIL_ATTACHMENT_FORMAT, LABEL_ATTACHMENT_FORMAT, DESCRIPTION_ATTACHMENT_FORMAT, attachmentFormats)
                                           .applyClearable(false)
                                           .applyRemoveSelected(true)
                                           .applyDefaultValue(EmailAttachmentFormat.NONE.name());

        return List.of(subjectLine, additionalEmailAddresses, additionalEmailAddressesOnly, projectOwnerOnly, attachmentFormat);
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
