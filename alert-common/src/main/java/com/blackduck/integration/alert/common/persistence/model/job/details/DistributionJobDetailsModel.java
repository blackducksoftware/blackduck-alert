/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.model.job.details;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKey;
import com.blackduck.integration.alert.api.descriptor.model.ChannelKeys;
import com.google.gson.annotations.JsonAdapter;

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
