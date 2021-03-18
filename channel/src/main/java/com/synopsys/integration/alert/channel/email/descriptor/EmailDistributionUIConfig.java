/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.descriptor;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.attachment.EmailAttachmentFormat;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.EndpointTableSelectField;
import com.synopsys.integration.alert.common.descriptor.config.field.endpoint.table.TableSelectColumn;
import com.synopsys.integration.alert.common.descriptor.config.field.validation.ValidationResult;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

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

    public EmailDistributionUIConfig() {
        super(ChannelKeys.EMAIL, EmailDescriptor.EMAIL_LABEL, EmailDescriptor.EMAIL_URL);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        ConfigField subjectLine = new TextInputConfigField(EmailDescriptor.KEY_SUBJECT_LINE, LABEL_SUBJECT_LINE, DESCRIPTION_EMAIL_SUBJECT_LINE);
        ConfigField additionalEmailAddresses = new EndpointTableSelectField(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES, LABEL_ADDITIONAL_ADDRESSES, DESCRIPTION_ADDITIONAL_ADDRESSES)
                                                   .applyColumn(TableSelectColumn.visible("emailAddress", "Email Address", true, true))
                                                   .applySearchable(true)
                                                   .applyPaged(true)
                                                   .applyRequiredRelatedField(ChannelDistributionUIConfig.KEY_PROVIDER_NAME)
                                                   .applyRequiredRelatedField(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);
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

    private ValidationResult validateAdditionalEmailAddressesOnly(FieldValueModel fieldToValidate, FieldModel fieldModel) {
        boolean useOnlyAdditionalEmailAddresses = fieldToValidate.getValue().map(Boolean::parseBoolean).orElse(false);
        if (useOnlyAdditionalEmailAddresses) {
            boolean hasAdditionalAddresses = fieldModel
                                                 .getFieldValueModel(EmailDescriptor.KEY_EMAIL_ADDITIONAL_ADDRESSES)
                                                 .map(FieldValueModel::getValues)
                                                 .filter(additionalEmailAddresses -> !additionalEmailAddresses.isEmpty())
                                                 .isPresent();
            if (!hasAdditionalAddresses) {
                return ValidationResult.errors("No additional email addresses were provided.");
            }
        }
        return ValidationResult.success();
    }

}
