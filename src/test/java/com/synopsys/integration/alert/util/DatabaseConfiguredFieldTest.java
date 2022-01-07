package com.synopsys.integration.alert.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.SlackJobDetailsModel;
import com.synopsys.integration.alert.database.configuration.repository.DescriptorConfigRepository;
import com.synopsys.integration.alert.database.configuration.repository.FieldValueRepository;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;

@Transactional
@AlertIntegrationTest
public abstract class DatabaseConfiguredFieldTest {
    @Autowired
    private JobAccessor jobAccessor;
    @Autowired
    private ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    @Autowired
    private DescriptorConfigRepository descriptorConfigRepository;

    @Autowired
    private FieldValueRepository fieldValueRepository;

    @BeforeEach
    @AfterEach
    public void initializeTest() {
        descriptorConfigRepository.flush();
        fieldValueRepository.flush();
        descriptorConfigRepository.deleteAllInBatch();
        fieldValueRepository.deleteAllInBatch();
    }

    public DistributionJobModel createAndSaveMockDistributionJob(Long blackDuckGlobalConfigId) {
        DistributionJobRequestModel jobRequestModel = createDistributionJobRequestModel(blackDuckGlobalConfigId);
        return addDistributionJob(jobRequestModel);
    }

    public DistributionJobModel addDistributionJob(DistributionJobRequestModel jobRequestModel) {
        return jobAccessor.createJob(jobRequestModel);
    }

    public ConfigurationModel addGlobalConfiguration(DescriptorKey descriptorKey, Map<String, Collection<String>> fieldsValues) {
        Set<ConfigurationFieldModel> fieldModels = fieldsValues
            .entrySet()
            .stream()
            .map(entry -> createConfigurationFieldModel(entry.getKey(), entry.getValue()))
            .collect(Collectors.toSet());

        return configurationModelConfigurationAccessor.createConfiguration(descriptorKey, ConfigContextEnum.GLOBAL, fieldModels);
    }

    public ConfigurationFieldModel createConfigurationFieldModel(String key, Collection<String> values) {
        ConfigurationFieldModel configurationFieldModel = BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY.equals(key) ? ConfigurationFieldModel.createSensitive(key) : ConfigurationFieldModel.create(key);
        configurationFieldModel.setFieldValues(values);
        return configurationFieldModel;
    }

    public ConfigurationModelConfigurationAccessor getConfigurationAccessor() {
        return configurationModelConfigurationAccessor;
    }

    private DistributionJobRequestModel createDistributionJobRequestModel(Long blackDuckGlobalConfigId) {
        SlackJobDetailsModel details = new SlackJobDetailsModel(null, "channel_webhook", "#channel-name", getClass().getSimpleName());
        return new DistributionJobRequestModel(
            true,
            getClass().getSimpleName() + " Test Job",
            FrequencyType.DAILY,
            ProcessingType.DEFAULT,
            ChannelKeys.SLACK.getUniversalKey(),
            blackDuckGlobalConfigId,
            false,
            null,
            null,
            List.of("notificationType"),
            List.of(),
            List.of(),
            List.of(),
            details
        );
    }

}
