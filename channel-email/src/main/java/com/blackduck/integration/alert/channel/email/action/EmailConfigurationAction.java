/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.alert.common.action.ConfigurationAction;

/**
 * @deprecated Global configuration actions for Email channel are now handled through EmailGlobalCrudActions
 */
@Component
@Deprecated(forRemoval = true)
public class EmailConfigurationAction extends ConfigurationAction {
    @Autowired
    protected EmailConfigurationAction(EmailGlobalFieldModelTestAction emailGlobalFieldModelTestAction) {
        super(ChannelKeys.EMAIL);
        addGlobalTestAction(emailGlobalFieldModelTestAction);
    }

}
