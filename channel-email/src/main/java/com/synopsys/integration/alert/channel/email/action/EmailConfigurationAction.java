/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class EmailConfigurationAction extends ConfigurationAction {
    @Autowired
    protected EmailConfigurationAction(EmailGlobalFieldModelTestAction emailGlobalFieldModelTestAction) {
        super(ChannelKeys.EMAIL);
        addGlobalTestAction(emailGlobalFieldModelTestAction);
    }

}
