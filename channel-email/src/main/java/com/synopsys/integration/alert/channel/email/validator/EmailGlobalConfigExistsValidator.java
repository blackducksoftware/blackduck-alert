package com.synopsys.integration.alert.channel.email.validator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.common.descriptor.config.ConcreteGlobalConfigExistsValidator;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

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
