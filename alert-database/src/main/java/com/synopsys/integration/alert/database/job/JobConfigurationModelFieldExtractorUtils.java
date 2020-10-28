package com.synopsys.integration.alert.database.job;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;

public class JobConfigurationModelFieldExtractorUtils {

    public static DistributionJobModel convertToDistributionJobModel(Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return DistributionJobModel.builder()
                   .enabled(extractFieldValue("channel.common.enabled", configuredFieldsMap).map(Boolean::valueOf).orElse(true))
                   .name(extractFieldValueOrEmptyString("channel.common.name", configuredFieldsMap))
                   .distributionFrequency(extractFieldValueOrEmptyString("channel.common.frequency", configuredFieldsMap))
                   .processingType(extractFieldValueOrEmptyString("provider.distribution.processing.type", configuredFieldsMap))
                   .channelDescriptorName(extractFieldValueOrEmptyString("channel.common.channel.name", configuredFieldsMap))
                   .blackDuckGlobalConfigId(extractFieldValue("provider.common.config.id", configuredFieldsMap).map(Long::valueOf).orElse(-1L))
                   .filterByProject(extractFieldValue("channel.common.filter.by.project", configuredFieldsMap).map(Boolean::valueOf).orElse(false))
                   .projectNamePattern(extractFieldValue("channel.common.project.name.pattern", configuredFieldsMap).orElse(null))
                   .notificationTypes(extractFieldValues("provider.distribution.notification.types", configuredFieldsMap))
                   .policyFilterPolicyNames(extractFieldValues("blackduck.policy.notification.filter", configuredFieldsMap))
                   .vulnerabilityFilterSeverityNames(extractFieldValues("blackduck.vulnerability.notification.filter", configuredFieldsMap))
                   .build();
    }

    public static String extractFieldValueOrEmptyString(String fieldKey, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return extractFieldValue(fieldKey, configuredFieldsMap).orElse("");
    }

    public static Optional<String> extractFieldValue(String fieldKey, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        return extractFieldValues(fieldKey, configuredFieldsMap)
                   .stream()
                   .findAny();
    }

    public static List<String> extractFieldValues(String fieldKey, Map<String, ConfigurationFieldModel> configuredFieldsMap) {
        ConfigurationFieldModel fieldModel = configuredFieldsMap.get(fieldKey);
        if (null != fieldModel) {
            return new ArrayList<>(fieldModel.getFieldValues());
        }
        return List.of();
    }

}
