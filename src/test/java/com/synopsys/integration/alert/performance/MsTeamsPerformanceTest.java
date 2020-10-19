package com.synopsys.integration.alert.performance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.msteams.descriptor.MsTeamsDescriptor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.performance.model.MsTeamsPerformanceProperties;
import com.synopsys.integration.exception.IntegrationException;

public class MsTeamsPerformanceTest extends IntegrationPerformanceTest {
    private final static String MS_TEAMS_PERFORMANCE_JOB_NAME = "MsTeams Performance Job";
    private final MsTeamsPerformanceProperties msTeamsProperties = new MsTeamsPerformanceProperties();

    @Test
    @Ignore
    public void testMsTeamsJob() throws Exception {
        runTest();
    }

    @Override
    public String createJob() throws IntegrationException {
        Map<String, FieldValueModel> providerKeyToValues = new HashMap<>();
        providerKeyToValues.put(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, new FieldValueModel(List.of(getBlackDuckProviderID()), true));
        providerKeyToValues.put(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES, new FieldValueModel(List.of("BOM_EDIT", "POLICY_OVERRIDE", "RULE_VIOLATION", "RULE_VIOLATION_CLEARED", "VULNERABILITY"), true));
        providerKeyToValues.put(ProviderDistributionUIConfig.KEY_PROCESSING_TYPE, new FieldValueModel(List.of(ProcessingType.DEFAULT.name()), true));
        providerKeyToValues.put(ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT, new FieldValueModel(List.of("true"), true));
        providerKeyToValues.put(ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT, new FieldValueModel(List.of(getBlackDuckProperties().getBlackDuckProjectName()), true));
        FieldModel jobProviderConfiguration = new FieldModel(getBlackDuckProperties().getBlackDuckProviderKey(), ConfigContextEnum.DISTRIBUTION.name(), providerKeyToValues);

        Map<String, FieldValueModel> msTeamsKeyToValues = new HashMap<>();
        msTeamsKeyToValues.put(ChannelDistributionUIConfig.KEY_ENABLED, new FieldValueModel(List.of("true"), true));
        msTeamsKeyToValues.put(ChannelDistributionUIConfig.KEY_CHANNEL_NAME, new FieldValueModel(List.of(msTeamsProperties.getMsTeamsChannelKey()), true));
        msTeamsKeyToValues.put(ChannelDistributionUIConfig.KEY_NAME, new FieldValueModel(List.of(getJobName()), true));
        msTeamsKeyToValues.put(ChannelDistributionUIConfig.KEY_FREQUENCY, new FieldValueModel(List.of(FrequencyType.REAL_TIME.name()), true));
        msTeamsKeyToValues.put(ChannelDistributionUIConfig.KEY_PROVIDER_NAME, new FieldValueModel(List.of(getBlackDuckProperties().getBlackDuckProviderKey()), true));

        msTeamsKeyToValues.put(MsTeamsDescriptor.KEY_WEBHOOK, new FieldValueModel(List.of(msTeamsProperties.getMsTeamsWebhook()), true));

        FieldModel jobSlackConfiguration = new FieldModel(msTeamsProperties.getMsTeamsChannelKey(), ConfigContextEnum.DISTRIBUTION.name(), msTeamsKeyToValues);

        JobFieldModel jobFieldModel = new JobFieldModel(null, Set.of(jobSlackConfiguration, jobProviderConfiguration));

        String jobConfigBody = getGson().toJson(jobFieldModel);

        getAlertRequestUtility().executePostRequest("/api/configuration/job/validate", jobConfigBody, String.format("Validating the Job %s failed.", getJobName()));
        getAlertRequestUtility().executePostRequest("/api/configuration/job/test", jobConfigBody, String.format("Testing the Job %s failed.", getJobName()));
        String creationResponse = getAlertRequestUtility().executePostRequest("/api/configuration/job", jobConfigBody, String.format("Could not create the Job %s.", getJobName()));

        JsonObject jsonObject = getGson().fromJson(creationResponse, JsonObject.class);
        return jsonObject.get("jobId").getAsString();
    }

    @Override
    public String getJobName() {
        return MS_TEAMS_PERFORMANCE_JOB_NAME;
    }

}
