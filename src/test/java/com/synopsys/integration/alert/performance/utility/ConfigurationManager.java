package com.synopsys.integration.alert.performance.utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.rest.model.JobPagedModel;
import com.synopsys.integration.alert.common.rest.model.JobProviderProjectFieldModel;
import com.synopsys.integration.blackduck.api.manual.enumeration.NotificationType;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.exception.IntegrationException;

public class ConfigurationManager {
    private final AlertRequestUtility alertRequestUtility;
    private final Gson gson;
    private final String blackDuckProviderKey;
    private final String channelKey;

    public ConfigurationManager(Gson gson, AlertRequestUtility alertRequestUtility, String blackDuckProviderKey, String channelKey) {
        this.gson = gson;
        this.alertRequestUtility = alertRequestUtility;
        this.blackDuckProviderKey = blackDuckProviderKey;
        this.channelKey = channelKey;
    }

    public void createGlobalConfiguration(FieldModel globalConfig) {
        String configurationRequestBody = gson.toJson(globalConfig);

        String descriptorName = globalConfig.getDescriptorName();
        try {
            String globalConfigSearchResponse = alertRequestUtility.executeGetRequest(
                String.format("/api/configuration?context=%s&descriptorName=%s", ConfigContextEnum.GLOBAL.name(), descriptorName),
                String.format("Could not find the existing global configuration for %s.", descriptorName));
            JsonObject globalConfigSearchJsonObject = gson.fromJson(globalConfigSearchResponse, JsonObject.class);
            JsonArray fieldModels = globalConfigSearchJsonObject.get("fieldModels").getAsJsonArray();
            JsonElement firstFieldModel = fieldModels.get(0);
            FieldModel originalGlobalConfig = gson.fromJson(firstFieldModel, FieldModel.class);

            globalConfig.setId(originalGlobalConfig.getId());

            alertRequestUtility.executePostRequest("/api/configuration/validate", configurationRequestBody, String.format("Validating the global configuration %s failed.", descriptorName));
            alertRequestUtility.executePostRequest("/api/configuration/test", configurationRequestBody, String.format("Testing the global configuration %s failed.", descriptorName));
            alertRequestUtility.executePutRequest(
                String.format("/api/configuration/%s", globalConfig.getId()), configurationRequestBody,
                String.format("Could not create the global configuration %s.", descriptorName));
        } catch (IntegrationException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public String createJob(Map<String, FieldValueModel> channelFields, String jobName, String blackDuckProviderId, String blackDuckProjectName) throws IntegrationException {
        return createJob(channelFields, jobName, blackDuckProviderId, blackDuckProjectName,
            List.of(NotificationType.BOM_EDIT, NotificationType.POLICY_OVERRIDE, NotificationType.RULE_VIOLATION, NotificationType.RULE_VIOLATION_CLEARED, NotificationType.VULNERABILITY));
    }

    public String createJob(Map<String, FieldValueModel> channelFields, String jobName, String blackDuckProviderId, String blackDuckProjectName, List<NotificationType> notificationTypes) throws IntegrationException {
        List<String> notificationTypeNames = notificationTypes.stream()
            .map(Enum::name)
            .collect(Collectors.toList());
        Map<String, FieldValueModel> providerKeyToValues = new HashMap<>();
        providerKeyToValues.put(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, new FieldValueModel(List.of(blackDuckProviderId), true));
        providerKeyToValues.put(ProviderDescriptor.KEY_NOTIFICATION_TYPES, new FieldValueModel(notificationTypeNames, true));
        providerKeyToValues.put(ProviderDescriptor.KEY_PROCESSING_TYPE, new FieldValueModel(List.of(ProcessingType.DEFAULT.name()), true));
        providerKeyToValues.put(ProviderDescriptor.KEY_FILTER_BY_PROJECT, new FieldValueModel(List.of("true"), true));
        providerKeyToValues.put(ProviderDescriptor.KEY_CONFIGURED_PROJECT, new FieldValueModel(List.of(blackDuckProjectName), true));
        FieldModel jobProviderConfiguration = new FieldModel(blackDuckProviderKey, ConfigContextEnum.DISTRIBUTION.name(), providerKeyToValues);

        FieldModel jobConfiguration = new FieldModel(channelKey, ConfigContextEnum.DISTRIBUTION.name(), channelFields);

        JobFieldModel jobFieldModel = new JobFieldModel(null, Set.of(jobConfiguration, jobProviderConfiguration), List.of(new JobProviderProjectFieldModel(blackDuckProjectName, "href", false)));

        String jobConfigBody = gson.toJson(jobFieldModel);

        alertRequestUtility.executePostRequest("/api/configuration/job/validate", jobConfigBody, String.format("Validating the Job %s failed.", jobName));
        alertRequestUtility.executePostRequest("/api/configuration/job/test", jobConfigBody, String.format("Testing the Job %s failed.", jobName));
        String creationResponse = alertRequestUtility.executePostRequest("/api/configuration/job", jobConfigBody, String.format("Could not create the Job %s.", jobName));

        JsonObject jsonObject = gson.fromJson(creationResponse, JsonObject.class);
        return jsonObject.get("jobId").getAsString();
    }

    public void copyJob(String jobToCopy, String newJobName) throws IntegrationException {
        String response = alertRequestUtility.executeGetRequest(String.format("/api/configuration/job?searchTerm=%s", jobToCopy), String.format("Could not copy the Job %s.", jobToCopy));
        JobPagedModel jobModel = gson.fromJson(response, JobPagedModel.class);
        JobFieldModel jobFieldModel = jobModel.getJobs().stream()
            .findFirst()
            .orElseThrow(() -> new AlertRuntimeException(String.format("Cannot find job %s", jobToCopy), null));

        jobFieldModel.setJobId(null);
        FieldModel channelFieldModel = jobFieldModel.getFieldModels().stream()
            .filter(model -> ChannelKeys.getChannelKey(model.getDescriptorName()) != null)
            .findFirst()
            .orElseThrow(() -> new AlertRuntimeException("Cannot find channel field model", null));
        Map<String, FieldValueModel> channelKeyToValues = new HashMap<>();
        channelKeyToValues.putAll(channelFieldModel.getKeyToValues());
        channelKeyToValues.put(ChannelDescriptor.KEY_NAME, new FieldValueModel(List.of(newJobName), true));
        channelFieldModel.setKeyToValues(channelKeyToValues);

        String jobConfigBody = gson.toJson(jobFieldModel);
        alertRequestUtility.executePostRequest("/api/configuration/job", jobConfigBody, String.format("Could not create the Job %s.", newJobName));
    }

}
