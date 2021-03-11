/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.jira2.server.action;

import com.synopsys.integration.alert.channel.api.DistributionChannelV2;
import com.synopsys.integration.alert.channel.api.action.DistributionChannelTestAction;
import com.synopsys.integration.alert.common.persistence.model.job.details.JiraServerJobDetailsModel;

// FIXME make this a component and autowire the strongly-typed channel in the constructor
//  @Component
public class JiraServerDistributionTestAction extends DistributionChannelTestAction<JiraServerJobDetailsModel> {
    // @Autowired
    public JiraServerDistributionTestAction(DistributionChannelV2<JiraServerJobDetailsModel> distributionChannel) {
        super(distributionChannel);
    }

}
