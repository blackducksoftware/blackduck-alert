/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.msteams.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.msteams2.action.MSTeamsDistributionTestAction;
import com.synopsys.integration.alert.common.action.ConfigurationAction;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class MsTeamsConfigurationAction extends ConfigurationAction {
    @Autowired
    protected MsTeamsConfigurationAction(MsTeamsJobDetailsExtractor msTeamsJobDetailsExtractor, MSTeamsDistributionTestAction msTeamsDistributionTestAction) {
        super(ChannelKeys.MS_TEAMS);
        addJobDetailsExtractor(msTeamsJobDetailsExtractor);
        addDistributionTestAction(msTeamsDistributionTestAction);
    }

}
