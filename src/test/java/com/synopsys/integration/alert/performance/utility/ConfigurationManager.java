package com.synopsys.integration.alert.performance.utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.synopsys.integration.alert.api.common.model.Obfuscated;
import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
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
import com.synopsys.integration.alert.api.descriptor.model.ChannelKeys;
import com.blackduck.integration.blackduck.api.manual.enumeration.NotificationType;
import com.blackduck.integration.blackduck.service.model.ProjectVersionWrapper;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.Slf4jIntLogger;

public class ConfigurationManager {
    private final IntLogger intLogger = new Slf4jIntLogger(LoggerFactory.getLogger(getClass()));
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

    public <T extends Obfuscated<T>> Optional<T> createGlobalConfiguration(String apiConfigurationPath, T globalConfigModel, Class<T> modelType) {
        try {
            String requestBody = gson.toJson(globalConfigModel);

            String validationResponseString = alertRequestUtility
                .executePostRequest(String.format("%s/validate", apiConfigurationPath), requestBody, "Validating the global configuration failed.");
            ValidationResponseModel validationResponse = gson.fromJson(validationResponseString, ValidationResponseModel.class);
            if (validationResponse.hasErrors()) {
                intLogger.error(String.format("Could not validate global configuration model. Error: %s", validationResponse.getErrors()));
                return Optional.empty();
            }
            String testResponseString = alertRequestUtility
                .executePostRequest(String.format("%s/test", apiConfigurationPath), requestBody, "Testing the global configuration failed.");
            ValidationResponseModel testResponse = gson.fromJson(testResponseString, ValidationResponseModel.class);
            if (testResponse.hasErrors() && !ValidationResponseModel.VALIDATION_SUCCESS_MESSAGE.equals(validationResponse.getMessage())) {
                intLogger.error(String.format("Testing the global config model error message: %s", validationResponse.getMessage()));
                intLogger.error(String.format("Testing the global config model failed. Error: %s", validationResponse.getErrors()));
                return Optional.empty();
            }

            String globalConfigCreateResponse = alertRequestUtility.executePostRequest(apiConfigurationPath, requestBody, "Could not create the global configuration");
            JsonObject globalConfigSearchJsonObject = gson.fromJson(globalConfigCreateResponse, JsonObject.class);
            T savedGlobalConfig = gson.fromJson(globalConfigSearchJsonObject, modelType);
            return Optional.of(savedGlobalConfig);
        } catch (IntegrationException e) {
            intLogger.error("Unexpected error occurred while creating the global configuration.", e);
            return Optional.empty();
        }
    }

    public String createJob(Map<String, FieldValueModel> channelFields, String jobName, String blackDuckProviderId, String blackDuckProjectName) throws IntegrationException {
        JobProviderProjectFieldModel providerProjectModel = new JobProviderProjectFieldModel(blackDuckProjectName, "href", false);
        return createJob(channelFields, jobName, blackDuckProviderId, List.of(providerProjectModel),
            List.of(
                NotificationType.BOM_EDIT,
                NotificationType.POLICY_OVERRIDE,
                NotificationType.RULE_VIOLATION,
                NotificationType.RULE_VIOLATION_CLEARED,
                NotificationType.VULNERABILITY
            )
        );
    }

    public String createJob(Map<String, FieldValueModel> channelFields, String jobName, String blackDuckProviderId, List<ProjectVersionWrapper> projectVersionWrappers)
        throws IntegrationException {
        List<JobProviderProjectFieldModel> providerProjectModels = projectVersionWrappers
            .stream()
            .map(ProjectVersionWrapper::getProjectView)
            .map(projectView -> new JobProviderProjectFieldModel(projectView.getName(), projectView.getHref().toString(), false))
            .collect(Collectors.toList());
        return createJob(channelFields, jobName, blackDuckProviderId, providerProjectModels,
            List.of(
                NotificationType.BOM_EDIT,
                NotificationType.POLICY_OVERRIDE,
                NotificationType.RULE_VIOLATION,
                NotificationType.RULE_VIOLATION_CLEARED,
                NotificationType.VULNERABILITY
            )
        );
    }

    public String createJob(
        Map<String, FieldValueModel> channelFields,
        String jobName,
        String blackDuckProviderId,
        List<JobProviderProjectFieldModel> providerProjectModel,
        List<NotificationType> notificationTypes
    ) throws IntegrationException {
        List<String> notificationTypeNames = notificationTypes.stream()
            .map(Enum::name)
            .collect(Collectors.toList());
        List<String> blackDuckProjectNames = providerProjectModel
            .stream()
            .map(JobProviderProjectFieldModel::getName)
            .collect(Collectors.toList());
        Map<String, FieldValueModel> providerKeyToValues = new HashMap<>();
        providerKeyToValues.put(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, new FieldValueModel(List.of(blackDuckProviderId), true));
        providerKeyToValues.put(ProviderDescriptor.KEY_NOTIFICATION_TYPES, new FieldValueModel(notificationTypeNames, true));
        providerKeyToValues.put(ProviderDescriptor.KEY_PROCESSING_TYPE, new FieldValueModel(List.of(ProcessingType.DEFAULT.name()), true));
        providerKeyToValues.put(ProviderDescriptor.KEY_FILTER_BY_PROJECT, new FieldValueModel(List.of("true"), true));
        providerKeyToValues.put(ProviderDescriptor.KEY_CONFIGURED_PROJECT, new FieldValueModel(blackDuckProjectNames, true));
        FieldModel jobProviderConfiguration = new FieldModel(blackDuckProviderKey, ConfigContextEnum.DISTRIBUTION.name(), providerKeyToValues);

        FieldModel jobConfiguration = new FieldModel(channelKey, ConfigContextEnum.DISTRIBUTION.name(), channelFields);

        JobFieldModel jobFieldModel = new JobFieldModel(
            null,
            Set.of(jobConfiguration, jobProviderConfiguration),
            providerProjectModel
        );

        String jobConfigBody = gson.toJson(jobFieldModel);

        alertRequestUtility.executePostRequest("/api/configuration/job/validate", jobConfigBody, String.format("Validating the Job %s failed.", jobName));
        alertRequestUtility.executePostRequest("/api/configuration/job/test", jobConfigBody, String.format("Testing the Job %s failed.", jobName));
        String creationResponse = alertRequestUtility.executePostRequest("/api/configuration/job", jobConfigBody, String.format("Could not create the Job %s.", jobName));

        JsonObject jsonObject = gson.fromJson(creationResponse, JsonObject.class);
        return jsonObject.get("jobId").getAsString();
    }

    public String createPolicyViolationJob(
        Map<String, FieldValueModel> channelFields,
        String jobName,
        String blackDuckProviderId
    ) throws IntegrationException {
        Map<String, FieldValueModel> providerKeyToValues = new HashMap<>();
        providerKeyToValues.put(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID, new FieldValueModel(List.of(blackDuckProviderId), true));
        providerKeyToValues.put(ProviderDescriptor.KEY_NOTIFICATION_TYPES, new FieldValueModel(List.of(NotificationType.RULE_VIOLATION.name()), true));
        providerKeyToValues.put(ProviderDescriptor.KEY_PROCESSING_TYPE, new FieldValueModel(List.of(ProcessingType.DEFAULT.name()), true));
        providerKeyToValues.put(ProviderDescriptor.KEY_FILTER_BY_PROJECT, new FieldValueModel(List.of("false"), true));
        FieldModel jobProviderConfiguration = new FieldModel(blackDuckProviderKey, ConfigContextEnum.DISTRIBUTION.name(), providerKeyToValues);

        FieldModel jobConfiguration = new FieldModel(channelKey, ConfigContextEnum.DISTRIBUTION.name(), channelFields);

        JobFieldModel jobFieldModel = new JobFieldModel(
            null,
            Set.of(jobConfiguration, jobProviderConfiguration),
            List.of()
        );

        String jobConfigBody = gson.toJson(jobFieldModel);

        alertRequestUtility.executePostRequest("/api/configuration/job/validate", jobConfigBody, String.format("Validating the Job %s failed.", jobName));
        alertRequestUtility.executePostRequest("/api/configuration/job/test", jobConfigBody, String.format("Testing the Job %s failed.", jobName));
        String creationResponse = alertRequestUtility.executePostRequest("/api/configuration/job", jobConfigBody, String.format("Could not create the Job %s.", jobName));

        JsonObject jsonObject = gson.fromJson(creationResponse, JsonObject.class);
        return jsonObject.get("jobId").getAsString();
    }

    public void copyJob(String jobToCopy, String newJobName) throws IntegrationException {
        String response = alertRequestUtility
            .executeGetRequest(String.format("/api/configuration/job?searchTerm=%s", jobToCopy), String.format("Could not copy the Job %s.", jobToCopy));
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

