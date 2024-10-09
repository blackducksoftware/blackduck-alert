package com.blackduck.integration.alert.channel.msteams.actions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.channel.action.DistributionChannelMessageTestAction;
import com.blackduck.integration.alert.api.descriptor.MsTeamsKey;
import com.blackduck.integration.alert.channel.msteams.distribution.MSTeamsChannel;
import com.blackduck.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;

@Component
public class MSTeamsDistributionTestAction extends DistributionChannelMessageTestAction<MSTeamsJobDetailsModel> {
    @Autowired
    public MSTeamsDistributionTestAction(MsTeamsKey channelKey, MSTeamsChannel distributionChannel) {
        super(channelKey, distributionChannel);
    }

}
