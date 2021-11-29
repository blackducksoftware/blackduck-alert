/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.descriptor;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.validator.EmailDistributionConfigurationValidator;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationValidator;
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

    private final EmailGlobalConfigurationValidator emailGlobalValidator;
    private final EmailDistributionConfigurationValidator emailDistributionValidator;

    @Autowired
    public EmailDescriptor(EmailGlobalUIConfig emailGlobalUIConfig, EmailDistributionUIConfig emailDistributionUIConfig, EmailGlobalConfigurationValidator emailGlobalValidator, EmailDistributionConfigurationValidator emailDistributionValidator) {
        super(ChannelKeys.EMAIL, emailDistributionUIConfig, emailGlobalUIConfig);
        this.emailGlobalValidator = emailGlobalValidator;
        this.emailDistributionValidator = emailDistributionValidator;
    }

    @Override
    public Optional<GlobalConfigurationValidator> getGlobalValidator() {
        return Optional.of(emailGlobalValidator);
    }

    @Override
    public Optional<DistributionConfigurationValidator> getDistributionValidator() {
        return Optional.of(emailDistributionValidator);
    }
}
