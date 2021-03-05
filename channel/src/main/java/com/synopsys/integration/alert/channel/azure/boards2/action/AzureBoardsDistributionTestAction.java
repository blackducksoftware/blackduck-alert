package com.synopsys.integration.alert.channel.azure.boards2.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.api.action.DistributionChannelTestAction;
import com.synopsys.integration.alert.channel.azure.boards2.AzureBoardsChannelV2;
import com.synopsys.integration.alert.common.channel.DistributionChannel;
import com.synopsys.integration.alert.common.persistence.model.job.details.AzureBoardsJobDetailsModel;

@Component
public class AzureBoardsDistributionTestAction extends DistributionChannelTestAction<AzureBoardsJobDetailsModel> {
    @Autowired
    public AzureBoardsDistributionTestAction(AzureBoardsChannelV2 distributionChannel) {
        super(distributionChannel);
    }

    @Override
    public DistributionChannel getDistributionChannel() {
        return null;
    }

}
