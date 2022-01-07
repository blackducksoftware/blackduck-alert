/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job.details;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.annotations.JsonAdapter;
import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@JsonAdapter(DistributionJobDetailsModelJsonAdapter.class)
public abstract class DistributionJobDetailsModel extends AlertSerializableModel {
    private static final long serialVersionUID = -9210491364879513303L;
    public static final Class<AzureBoardsJobDetailsModel> AZURE = AzureBoardsJobDetailsModel.class;
    public static final Class<EmailJobDetailsModel> EMAIL = EmailJobDetailsModel.class;
    public static final Class<JiraCloudJobDetailsModel> JIRA_CLOUD = JiraCloudJobDetailsModel.class;
    public static final Class<JiraServerJobDetailsModel> JIRA_SERVER = JiraServerJobDetailsModel.class;
    public static final Class<MSTeamsJobDetailsModel> MS_TEAMS = MSTeamsJobDetailsModel.class;
    public static final Class<SlackJobDetailsModel> SLACK = SlackJobDetailsModel.class;

    private static final Map<ChannelKey, Class<? extends DistributionJobDetailsModel>> detailsModels = new HashMap<>();

    static {
        detailsModels.put(ChannelKeys.AZURE_BOARDS, AZURE);
        detailsModels.put(ChannelKeys.EMAIL, EMAIL);
        detailsModels.put(ChannelKeys.JIRA_CLOUD, JIRA_CLOUD);
        detailsModels.put(ChannelKeys.JIRA_SERVER, JIRA_SERVER);
        detailsModels.put(ChannelKeys.MS_TEAMS, MS_TEAMS);
        detailsModels.put(ChannelKeys.SLACK, SLACK);
    }

    public static Class<? extends DistributionJobDetailsModel> getConcreteClass(ChannelKey channelKey) {
        return detailsModels.get(channelKey);
    }

    private final ChannelKey channelKey;
    private final UUID jobId;

    protected DistributionJobDetailsModel(ChannelKey channelKey, UUID jobId) {
        this.channelKey = channelKey;
        this.jobId = jobId;
    }

    public boolean isA(ChannelKey channelKey) {
        return this.channelKey.equals(channelKey);
    }

    public <T extends DistributionJobDetailsModel> T getAs(Class<T> clazz) {
        return clazz.cast(this);
    }

    public UUID getJobId() {
        return jobId;
    }

}
