/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.EmailChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.DescriptorKey;
import com.blackduck.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.blackduck.integration.alert.common.descriptor.config.ConcreteGlobalConfigExistsValidator;

@Component
public class EmailGlobalConfigExistsValidator implements ConcreteGlobalConfigExistsValidator {
    private final EmailChannelKey emailChannelKey;
    private final EmailGlobalConfigAccessor emailGlobalConfigAccessor;

    @Autowired
    public EmailGlobalConfigExistsValidator(
        EmailChannelKey emailChannelKey,
        EmailGlobalConfigAccessor emailGlobalConfigAccessor
    ) {
        this.emailChannelKey = emailChannelKey;
        this.emailGlobalConfigAccessor = emailGlobalConfigAccessor;
    }

    @Override
    public boolean exists() {
        return emailGlobalConfigAccessor.doesConfigurationExist();
    }

    @Override
    public DescriptorKey getDescriptorKey() {
        return emailChannelKey;
    }
}
