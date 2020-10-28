package com.synopsys.integration.alert.database.job;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.mutable.ConfigurationModelMutable;
import com.synopsys.integration.alert.common.provider.ProviderKey;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.configuration.RegisteredDescriptorEntity;
import com.synopsys.integration.alert.database.configuration.repository.RegisteredDescriptorRepository;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsEntity;
import com.synopsys.integration.alert.database.job.blackduck.BlackDuckJobDetailsRepository;
import com.synopsys.integration.alert.database.job.blackduck.notification.BlackDuckJobNotificationTypeEntity;
import com.synopsys.integration.alert.database.job.blackduck.notification.BlackDuckJobNotificationTypeRepository;
import com.synopsys.integration.alert.database.job.blackduck.policy.BlackDuckJobPolicyFilterEntity;
import com.synopsys.integration.alert.database.job.blackduck.policy.BlackDuckJobPolicyFilterRepository;
import com.synopsys.integration.alert.database.job.blackduck.projects.BlackDuckJobProjectEntity;
import com.synopsys.integration.alert.database.job.blackduck.projects.BlackDuckJobProjectRepository;
import com.synopsys.integration.alert.database.job.blackduck.vulnerability.BlackDuckJobVulnerabilitySeverityFilterEntity;
import com.synopsys.integration.alert.database.job.blackduck.vulnerability.BlackDuckJobVulnerabilitySeverityFilterRepository;

// @Component
public class StaticJobAccessor implements JobAccessor {
    private final DistributionJobRepository distributionJobRepository;
    private final BlackDuckJobDetailsRepository blackDuckJobDetailsRepository;
    private final BlackDuckJobNotificationTypeRepository blackDuckJobNotificationTypeRepository;
    private final BlackDuckJobProjectRepository blackDuckJobProjectRepository;
    private final BlackDuckJobPolicyFilterRepository blackDuckJobPolicyFilterRepository;
    private final BlackDuckJobVulnerabilitySeverityFilterRepository blackDuckJobVulnerabilitySeverityFilterRepository;

    // Temporary until all three tiers of the application have been updated to new Job models
    private final RegisteredDescriptorRepository registeredDescriptorRepository;
    // BlackDuck is currently the only provider, so this is safe in the short-term while we transition to new models
    private final ProviderKey blackDuckProviderKey;

    @Autowired
    public StaticJobAccessor(
        DistributionJobRepository distributionJobRepository,
        BlackDuckJobDetailsRepository blackDuckJobDetailsRepository,
        BlackDuckJobNotificationTypeRepository blackDuckJobNotificationTypeRepository,
        BlackDuckJobProjectRepository blackDuckJobProjectRepository,
        BlackDuckJobPolicyFilterRepository blackDuckJobPolicyFilterRepository,
        BlackDuckJobVulnerabilitySeverityFilterRepository blackDuckJobVulnerabilitySeverityFilterRepository,

        RegisteredDescriptorRepository registeredDescriptorRepository,
        ProviderKey blackDuckProviderKey
    ) {
        this.distributionJobRepository = distributionJobRepository;
        this.blackDuckJobDetailsRepository = blackDuckJobDetailsRepository;
        this.blackDuckJobNotificationTypeRepository = blackDuckJobNotificationTypeRepository;
        this.blackDuckJobProjectRepository = blackDuckJobProjectRepository;
        this.blackDuckJobPolicyFilterRepository = blackDuckJobPolicyFilterRepository;
        this.blackDuckJobVulnerabilitySeverityFilterRepository = blackDuckJobVulnerabilitySeverityFilterRepository;
        this.registeredDescriptorRepository = registeredDescriptorRepository;
        this.blackDuckProviderKey = blackDuckProviderKey;
    }

    @Override
    public List<ConfigurationJobModel> getAllJobs() {
        return distributionJobRepository.findAll()
                   .stream()
                   .map(this::convertToConfigurationJobModel)
                   .collect(Collectors.toList());
    }

    @Override
    public List<ConfigurationJobModel> getJobsById(Collection<UUID> jobIds) {
        return distributionJobRepository.findAllById(jobIds)
                   .stream()
                   .map(this::convertToConfigurationJobModel)
                   .collect(Collectors.toList());
    }

    @Override
    public AlertPagedModel<ConfigurationJobModel> getPageOfJobs(int pageOffset, int pageLimit, Collection<String> descriptorsNamesToInclude) {
        if (!descriptorsNamesToInclude.contains(blackDuckProviderKey.getUniversalKey())) {
            return new AlertPagedModel<>(0, pageOffset, pageLimit, List.of());
        }

        PageRequest pageRequest = PageRequest.of(pageOffset, pageLimit);
        Page<ConfigurationJobModel> pageOfJobsWithDescriptorNames = distributionJobRepository.findByChannelDescriptorNameIn(descriptorsNamesToInclude, pageRequest)
                                                                        .map(this::convertToConfigurationJobModel);
        return new AlertPagedModel<>(pageOfJobsWithDescriptorNames.getTotalPages(), pageOffset, pageLimit, pageOfJobsWithDescriptorNames.getContent());
    }

    @Override
    public Optional<ConfigurationJobModel> getJobById(UUID jobId) {
        return distributionJobRepository.findById(jobId).map(this::convertToConfigurationJobModel);
    }

    @Override
    public Optional<ConfigurationJobModel> getJobByName(String jobName) {
        return distributionJobRepository.findByName(jobName)
                   .map(this::convertToConfigurationJobModel);
    }

    @Override
    public List<ConfigurationJobModel> getJobsByFrequency(FrequencyType frequency) {
        return distributionJobRepository.findByDistributionFrequency(frequency.name())
                   .stream()
                   .map(this::convertToConfigurationJobModel)
                   .collect(Collectors.toList());
    }

    @Override
    public ConfigurationJobModel createJob(Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        Map<String, ConfigurationFieldModel> configuredFieldsMap = configuredFields
                                                                       .stream()
                                                                       .collect(Collectors.toMap(ConfigurationFieldModel::getFieldKey, Function.identity()));

        DistributionJobModel distributionJobModel = JobConfigurationModelFieldExtractorUtils.convertToDistributionJobModel(configuredFieldsMap);
        DistributionJobEntity jobToSave = new DistributionJobEntity(
            null,
            distributionJobModel.getName(),
            distributionJobModel.isEnabled(),
            distributionJobModel.getDistributionFrequency(),
            distributionJobModel.getProcessingType(),
            distributionJobModel.getChannelDescriptorName(),
            DateUtils.createCurrentDateTimestamp(),
            null
        );
        DistributionJobEntity savedJobEntity = distributionJobRepository.save(jobToSave);
        UUID savedJobId = savedJobEntity.getJobId();

        BlackDuckJobDetailsEntity savedBlackDuckJobDetails = saveBlackDuckJobDetails(savedJobId, distributionJobModel);
        savedJobEntity.setBlackDuckJobDetails(savedBlackDuckJobDetails);

        // FIXME save channel details

        return convertToConfigurationJobModel(savedJobEntity);
    }

    @Override
    public ConfigurationJobModel updateJob(UUID jobId, Collection<String> descriptorNames, Collection<ConfigurationFieldModel> configuredFields) throws AlertDatabaseConstraintException {
        DistributionJobEntity existingJob = distributionJobRepository.findById(jobId)
                                                .orElseThrow(() -> new AlertDatabaseConstraintException(String.format("No job exists with the id [%s]", jobId.toString())));
        // FIXME implement
        return null;
    }

    @Override
    public void deleteJob(UUID jobId) {
        distributionJobRepository.deleteById(jobId);
    }

    private ConfigurationJobModel convertToConfigurationJobModel(DistributionJobEntity jobEntity) {
        Set<ConfigurationModel> configurationModels = new LinkedHashSet<>();

        String createdAtDateTime = DateUtils.formatDate(jobEntity.getCreatedAt(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);
        String updatedAtDateTime = DateUtils.formatDate(jobEntity.getLastUpdated(), DateUtils.UTC_DATE_FORMAT_TO_MINUTE);

        String providerUniversalKey = blackDuckProviderKey.getUniversalKey();
        Long blackDuckDescriptorId = getDescriptorId(providerUniversalKey);

        ConfigurationModelMutable blackDuckConfigurationModel = new ConfigurationModelMutable(blackDuckDescriptorId, -1L, createdAtDateTime, updatedAtDateTime, ConfigContextEnum.DISTRIBUTION);
        JobConfigurationModelFieldPopulationUtils.populateBlackDuckConfigurationModelFields(jobEntity, blackDuckConfigurationModel);
        configurationModels.add(blackDuckConfigurationModel);

        Long channelDescriptorId = getDescriptorId(jobEntity.getChannelDescriptorName());
        ConfigurationModelMutable channelConfigurationModel = new ConfigurationModelMutable(channelDescriptorId, -1L, createdAtDateTime, updatedAtDateTime, ConfigContextEnum.DISTRIBUTION);
        channelConfigurationModel.put(JobConfigurationModelFieldPopulationUtils.createConfigFieldModel("channel.common.provider.name", providerUniversalKey));
        JobConfigurationModelFieldPopulationUtils.populateChannelConfigurationModelFields(jobEntity, channelConfigurationModel);
        configurationModels.add(channelConfigurationModel);

        return new ConfigurationJobModel(jobEntity.getJobId(), configurationModels);
    }

    private Long getDescriptorId(String descriptorUniversalKey) {
        return registeredDescriptorRepository.findFirstByName(descriptorUniversalKey)
                   .map(RegisteredDescriptorEntity::getId)
                   .orElseThrow(() -> new AlertRuntimeException("A descriptor is missing from the database"));
    }

    private BlackDuckJobDetailsEntity saveBlackDuckJobDetails(UUID jobId, DistributionJobModel distributionJobModel) {
        BlackDuckJobDetailsEntity blackDuckJobDetailsToSave = new BlackDuckJobDetailsEntity(
            jobId,
            distributionJobModel.getBlackDuckGlobalConfigId(),
            distributionJobModel.isFilterByProject(),
            distributionJobModel.getProjectNamePattern().orElse(null)
        );
        BlackDuckJobDetailsEntity savedBlackDuckJobDetails = blackDuckJobDetailsRepository.save(blackDuckJobDetailsToSave);

        List<BlackDuckJobNotificationTypeEntity> notificationTypesToSave = distributionJobModel.getNotificationTypes()
                                                                               .stream()
                                                                               .map(notificationType -> new BlackDuckJobNotificationTypeEntity(jobId, notificationType))
                                                                               .collect(Collectors.toList());
        List<BlackDuckJobNotificationTypeEntity> savedNotificationTypes = blackDuckJobNotificationTypeRepository.saveAll(notificationTypesToSave);
        savedBlackDuckJobDetails.setBlackDuckJobNotificationTypes(savedNotificationTypes);

        List<BlackDuckJobProjectEntity> ProjectFiltersToSave = distributionJobModel.getProjectFilterProjectNames()
                                                                   .stream()
                                                                   .map(projectName -> new BlackDuckJobProjectEntity(jobId, projectName))
                                                                   .collect(Collectors.toList());
        List<BlackDuckJobProjectEntity> savedProjectFilters = blackDuckJobProjectRepository.saveAll(ProjectFiltersToSave);
        savedBlackDuckJobDetails.setBlackDuckJobProjects(savedProjectFilters);

        List<BlackDuckJobPolicyFilterEntity> policyFiltersToSave = distributionJobModel.getPolicyFilterPolicyNames()
                                                                       .stream()
                                                                       .map(policyName -> new BlackDuckJobPolicyFilterEntity(jobId, policyName))
                                                                       .collect(Collectors.toList());
        List<BlackDuckJobPolicyFilterEntity> savedPolicyFilters = blackDuckJobPolicyFilterRepository.saveAll(policyFiltersToSave);
        savedBlackDuckJobDetails.setBlackDuckJobPolicyFilters(savedPolicyFilters);

        List<BlackDuckJobVulnerabilitySeverityFilterEntity> vulnerabilitySeverityFiltersToSave = distributionJobModel.getVulnerabilityFilterSeverityNames()
                                                                                                     .stream()
                                                                                                     .map(severityName -> new BlackDuckJobVulnerabilitySeverityFilterEntity(jobId, severityName))
                                                                                                     .collect(Collectors.toList());
        List<BlackDuckJobVulnerabilitySeverityFilterEntity> savedVulnerabilitySeverityFilters = blackDuckJobVulnerabilitySeverityFilterRepository.saveAll(vulnerabilitySeverityFiltersToSave);
        savedBlackDuckJobDetails.setBlackDuckJobVulnerabilitySeverityFilters(savedVulnerabilitySeverityFilters);

        return savedBlackDuckJobDetails;
    }

}
