package com.blackduck.integration.alert.api.channel;

import java.util.List;

import com.blackduck.integration.alert.api.common.model.exception.AlertException;
import com.blackduck.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;

public interface ChannelMessageSender<D extends DistributionJobDetailsModel, M, R> {
    R sendMessages(D details, List<M> channelMessages) throws AlertException;

}
