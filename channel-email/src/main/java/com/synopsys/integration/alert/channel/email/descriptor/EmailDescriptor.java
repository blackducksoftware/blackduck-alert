/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.descriptor;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.validator.EmailDistributionConfigurationValidator;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class EmailDescriptor extends ChannelDescriptor {
    public static final String EMAIL_PREFIX = "email.";

    public static final String KEY_SUBJECT_LINE = EMAIL_PREFIX + "subject.line";
    public static final String KEY_PROJECT_OWNER_ONLY = "project.owner.only";
    public static final String KEY_EMAIL_ADDRESSES = EMAIL_PREFIX + "addresses";
    public static final String KEY_EMAIL_ADDITIONAL_ADDRESSES = EMAIL_PREFIX + "additional.addresses";
    public static final String KEY_EMAIL_ADDITIONAL_ADDRESSES_ONLY = EMAIL_PREFIX + "additional.addresses.only";
    public static final String KEY_EMAIL_ATTACHMENT_FORMAT = EMAIL_PREFIX + "attachment.format";

    public static final String EMAIL_LABEL = "Email";
    public static final String EMAIL_URL = "email";
    public static final String EMAIL_DESCRIPTION = "Configure the email server that Alert will send emails to.";

    public static final String LABEL_SUBJECT_LINE = "Subject Line";
    public static final String LABEL_ADDITIONAL_ADDRESSES = "Additional Email Addresses";
    public static final String LABEL_ADDITIONAL_ADDRESSES_ONLY = "Additional Email Addresses Only";
    public static final String LABEL_PROJECT_OWNER_ONLY = "Project Owner Only";
    public static final String LABEL_ATTACHMENT_FORMAT = "Attachment File Type";

    private final EmailGlobalConfigurationFieldModelValidator emailGlobalValidator;
    private final EmailDistributionConfigurationValidator emailDistributionValidator;

    @Autowired
    public EmailDescriptor(EmailGlobalConfigurationFieldModelValidator emailGlobalValidator, EmailDistributionConfigurationValidator emailDistributionValidator) {
        super(ChannelKeys.EMAIL, Set.of(ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION));
        this.emailGlobalValidator = emailGlobalValidator;
        this.emailDistributionValidator = emailDistributionValidator;
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.of(emailGlobalValidator);
    }

    @Override
    public Optional<DistributionConfigurationValidator> getDistributionValidator() {
        return Optional.of(emailDistributionValidator);
    }

}
