/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.job;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.provider.ProviderProjectExistencePopulator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.FieldModelTestAction;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.action.api.AbstractJobResourceActions;
import com.synopsys.integration.alert.common.channel.DistributionChannelTestAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.common.descriptor.action.DescriptorActionMap;
import com.synopsys.integration.alert.common.descriptor.config.GlobalConfigExistsValidator;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.PermissionKey;
import com.synopsys.integration.alert.common.persistence.model.job.BlackDuckProjectDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobRequestModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.FieldModelProcessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldStatuses;
import com.synopsys.integration.alert.common.rest.model.JobIdsRequestModel;
import com.synopsys.integration.alert.common.rest.model.JobPagedModel;
import com.synopsys.integration.alert.common.rest.model.JobProviderProjectFieldModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.component.certificates.web.PKIXErrorResponseFactory;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

@Component
public class JobConfigActions extends AbstractJobResourceActions {
    private final Logger logger = LoggerFactory.getLogger(JobConfigActions.class);

    private final ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;
    private final JobAccessor jobAccessor;
    private final FieldModelProcessor fieldModelProcessor;
    private final DescriptorProcessor descriptorProcessor;
    private final ConfigurationFieldModelConverter modelConverter;
    private final GlobalConfigExistsValidator globalConfigExistsValidator;
    private final PKIXErrorResponseFactory pkixErrorResponseFactory;
    private final DescriptorActionMap<DistributionChannelTestAction> channelTestActionMap;
    private final DistributionJobModelExtractor distributionJobModelExtractor;

    private final ProviderProjectExistencePopulator providerProjectExistencePopulator;

    @Autowired
    public JobConfigActions(
        AuthorizationManager authorizationManager,
        DescriptorAccessor descriptorAccessor,
        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor,
        JobAccessor jobAccessor,
        FieldModelProcessor fieldModelProcessor,
        DescriptorProcessor descriptorProcessor,
        ConfigurationFieldModelConverter modelConverter,
        GlobalConfigExistsValidator globalConfigExistsValidator,
        PKIXErrorResponseFactory pkixErrorResponseFactory,
        DescriptorMap descriptorMap,
        ProviderProjectExistencePopulator providerProjectExistencePopulator,
        List<DistributionChannelTestAction> channelTestActions,
        DistributionJobModelExtractor distributionJobModelExtractor
    ) {
        super(authorizationManager, descriptorAccessor, descriptorMap);
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
        this.fieldModelProcessor = fieldModelProcessor;
        this.descriptorProcessor = descriptorProcessor;
        this.modelConverter = modelConverter;
        this.globalConfigExistsValidator = globalConfigExistsValidator;
        this.pkixErrorResponseFactory = pkixErrorResponseFactory;
        this.jobAccessor = jobAccessor;
        this.providerProjectExistencePopulator = providerProjectExistencePopulator;
        this.channelTestActionMap = new DescriptorActionMap<>(channelTestActions);
        this.distributionJobModelExtractor = distributionJobModelExtractor;
    }

    @Override
    public final ActionResponse<JobPagedModel> readPageWithoutChecks(Integer pageNumber, Integer pageSize, String searchTerm, Collection<String> permittedDescriptorsForSession) {
        AlertPagedModel<DistributionJobModel> pageOfJobs = jobAccessor.getPageOfJobs(pageNumber, pageSize, searchTerm, permittedDescriptorsForSession);
        List<DistributionJobModel> distributionJobModels = pageOfJobs.getModels();

        List<JobFieldModel> jobFieldModels = new ArrayList<>(distributionJobModels.size());
        for (DistributionJobModel distributionJobModel : distributionJobModels) {
            JobFieldModel jobFieldModel = JobFieldModelPopulationUtils.createJobFieldModelWithDefaultProviderProjectState(distributionJobModel);
            jobFieldModels.add(jobFieldModel);
        }

        JobPagedModel jobPagedModel = new JobPagedModel(pageOfJobs.getTotalPages(), pageOfJobs.getCurrentPage(), pageOfJobs.getPageSize(), jobFieldModels);
        return new ActionResponse<>(HttpStatus.OK, jobPagedModel);
    }

    @Override
    protected Optional<JobFieldModel> findJobFieldModel(UUID id) {
        Optional<DistributionJobModel> optionalJob = jobAccessor.getJobById(id);
        if (optionalJob.isPresent()) {
            DistributionJobModel distributionJobModel = optionalJob.get();
            List<JobProviderProjectFieldModel> jobProviderProjects = JobFieldModelPopulationUtils.createJobProviderProjects(distributionJobModel);
            providerProjectExistencePopulator.populateJobProviderProjects(distributionJobModel.getBlackDuckGlobalConfigId(), jobProviderProjects);
            JobFieldModel jobFieldModel = JobFieldModelPopulationUtils.createJobFieldModel(distributionJobModel, jobProviderProjects);
            return Optional.of(jobFieldModel);
        }
        return Optional.empty();
    }

    @Override
    protected ActionResponse<JobFieldModel> deleteWithoutChecks(UUID id) {
        try {
            Optional<DistributionJobModel> job = jobAccessor.getJobById(id);
            if (job.isPresent()) {
                LinkedList<FieldModel> processedFieldModels = new LinkedList<>();
                DistributionJobModel distributionJobModel = job.get();
                JobFieldModel jobFieldModel = JobFieldModelPopulationUtils.createJobFieldModelWithDefaultProviderProjectState(distributionJobModel);
                for (FieldModel fieldModel : jobFieldModel.getFieldModels()) {
                    FieldModel preProcessedFieldModel = fieldModelProcessor.performBeforeDeleteAction(fieldModel);
                    processedFieldModels.add(preProcessedFieldModel);
                }
                jobAccessor.deleteJob(distributionJobModel.getJobId());
                for (FieldModel preProcessedFieldModel : processedFieldModels) {
                    fieldModelProcessor.performAfterDeleteAction(preProcessedFieldModel);
                }
            } else {
                return new ActionResponse<>(HttpStatus.NOT_FOUND);
            }
        } catch (AlertException ex) {
            logger.error("Error reading job", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }

    @Override
    protected ActionResponse<JobFieldModel> createWithoutChecks(JobFieldModel resource) {
        try {
            Set<ConfigurationFieldModel> configurationFieldModels = new HashSet<>();
            for (FieldModel fieldModel : resource.getFieldModels()) {
                FieldModel beforeSaveEventFieldModel = fieldModelProcessor.performBeforeSaveAction(fieldModel);
                Collection<ConfigurationFieldModel> savedFieldsModels = modelConverter.convertToConfigurationFieldModelMap(beforeSaveEventFieldModel).values();
                configurationFieldModels.addAll(savedFieldsModels);
            }

            List<JobProviderProjectFieldModel> configuredProviderProjects = Optional.ofNullable(resource.getConfiguredProviderProjects()).orElse(List.of());
            DistributionJobRequestModel jobRequestModel = createDistributionJobRequestModel(configurationFieldModels, configuredProviderProjects, DateUtils.createCurrentDateTimestamp(), null);
            DistributionJobModel savedJob = jobAccessor.createJob(jobRequestModel);
            JobFieldModel savedJobFieldModel = JobFieldModelPopulationUtils.createJobFieldModel(savedJob, configuredProviderProjects);

            Set<FieldModel> updatedFieldModels = new HashSet<>();
            for (FieldModel fieldModel : savedJobFieldModel.getFieldModels()) {
                FieldModel updatedModel = fieldModelProcessor.performAfterSaveAction(fieldModel);
                updatedFieldModels.add(updatedModel);
            }
            savedJobFieldModel.setFieldModels(updatedFieldModels);
            return new ActionResponse<>(HttpStatus.OK, savedJobFieldModel);
        } catch (AlertException ex) {
            logger.error("Error creating job", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    @Override
    protected ActionResponse<JobFieldModel> updateWithoutChecks(UUID id, JobFieldModel resource) {
        try {
            Optional<DistributionJobModel> jobModel = jobAccessor.getJobById(id);
            if (jobModel.isPresent()) {
                DistributionJobModel previousJob = jobModel.get();
                JobFieldModel jobFieldModel = JobFieldModelPopulationUtils.createJobFieldModelWithDefaultProviderProjectState(previousJob);

                Map<String, FieldModel> descriptorAndContextToPreviousFieldModel = new HashMap<>();
                for (FieldModel previousJobFieldModel : jobFieldModel.getFieldModels()) {
                    descriptorAndContextToPreviousFieldModel.put(previousJobFieldModel.getDescriptorName() + previousJobFieldModel.getContext(), previousJobFieldModel);
                }

                Set<ConfigurationFieldModel> configurationFieldModels = new HashSet<>();
                for (FieldModel fieldModel : resource.getFieldModels()) {
                    FieldModel beforeUpdateEventFieldModel = fieldModelProcessor.performBeforeUpdateAction(fieldModel);
                    String beforeFieldModelId = beforeUpdateEventFieldModel.getId();
                    Long fieldModelId = (StringUtils.isNotBlank(beforeFieldModelId)) ? Long.parseLong(beforeFieldModelId) : null;
                    Collection<ConfigurationFieldModel> updatedFieldModels = fieldModelProcessor.fillFieldModelWithExistingData(fieldModelId, beforeUpdateEventFieldModel);
                    configurationFieldModels.addAll(updatedFieldModels);
                }

                List<JobProviderProjectFieldModel> configuredProviderProjects = Optional.ofNullable(resource.getConfiguredProviderProjects()).orElse(List.of());
                DistributionJobRequestModel jobRequestModel = createDistributionJobRequestModel(configurationFieldModels, configuredProviderProjects, previousJob.getCreatedAt(), DateUtils.createCurrentDateTimestamp());
                DistributionJobModel savedJob = jobAccessor.updateJob(previousJob.getJobId(), jobRequestModel);
                JobFieldModel savedJobFieldModel = JobFieldModelPopulationUtils.createJobFieldModel(savedJob, configuredProviderProjects);

                Set<FieldModel> updatedFieldModels = new HashSet<>();
                for (FieldModel fieldModel : savedJobFieldModel.getFieldModels()) {
                    FieldModel previousFieldModel = descriptorAndContextToPreviousFieldModel.get(fieldModel.getDescriptorName() + fieldModel.getContext());
                    FieldModel updatedModel = fieldModelProcessor.performAfterUpdateAction(previousFieldModel, fieldModel);
                    updatedFieldModels.add(updatedModel);
                }
                savedJobFieldModel.setFieldModels(updatedFieldModels);
                return new ActionResponse<>(HttpStatus.OK, savedJobFieldModel);
            } else {
                return new ActionResponse<>(HttpStatus.NOT_FOUND);
            }
        } catch (AlertException ex) {
            logger.error("Error creating job", ex);
            return new ActionResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        }
    }

    private Optional<AlertFieldStatus> validateJobNameUnique(@Nullable UUID currentJobId, JobFieldModel jobFieldModel) {
        return jobFieldModel.getFieldModels().stream()
            .filter(fieldModel -> fieldModel.getFieldValueModel(ChannelDescriptor.KEY_NAME).isPresent())
            .findFirst()
            .flatMap(fieldModel -> fieldModel.getFieldValueModel(ChannelDescriptor.KEY_NAME))
            .flatMap(fieldValueModel -> validateJobNameUnique(currentJobId, fieldValueModel));
    }

    private Optional<AlertFieldStatus> validateJobNameUnique(@Nullable UUID currentJobId, FieldValueModel fieldValueModel) {
        Optional<String> optionalJobName = fieldValueModel.getValue();
        if (optionalJobName.isPresent()) {
            String jobName = optionalJobName.get();
            // Because of FieldValueModel empty values aren't saved, therefore we don't need to check for empty values
            // Find an existing job with the name that does not have the same id as currentJobId.
            boolean foundDuplicateName = jobAccessor.getJobByName(jobName)
                .filter(job -> !job.getJobId().equals(currentJobId))
                .isPresent();
            if (foundDuplicateName) {
                return Optional.of(AlertFieldStatus.error(ChannelDescriptor.KEY_NAME, "A distribution configuration with this name already exists."));
            }
        }
        return Optional.empty();
    }

    @Override
    protected ValidationActionResponse validateWithoutChecks(JobFieldModel resource) {
        UUID jobId = null;
        if (StringUtils.isNotBlank(resource.getJobId())) {
            jobId = UUID.fromString(resource.getJobId());
        }
        List<AlertFieldStatus> fieldStatuses = new ArrayList<>();

        validateJobNameUnique(jobId, resource).ifPresent(fieldStatuses::add);

        List<AlertFieldStatus> descriptorValidationResults = validateDescriptorFields(resource);
        fieldStatuses.addAll(descriptorValidationResults);

        if (!fieldStatuses.isEmpty()) {
            ValidationResponseModel responseModel = ValidationResponseModel.fromStatusCollection("Invalid Configuration", fieldStatuses);
            return new ValidationActionResponse(HttpStatus.BAD_REQUEST, responseModel);
        }

        ValidationResponseModel responseModel = ValidationResponseModel.success("Valid");
        return new ValidationActionResponse(HttpStatus.OK, responseModel);
    }

    public ActionResponse<List<JobFieldStatuses>> validateJobsById(JobIdsRequestModel jobIdsValidationModel) {
        List<PermissionKey> keys = gatherPermissionKeys();
        if (!getAuthorizationManager().anyReadPermission(keys)) {
            return ActionResponse.createForbiddenResponse();
        }

        List<UUID> jobIdsToValidate = jobIdsValidationModel.getJobIds();
        if (null == jobIdsToValidate || jobIdsToValidate.isEmpty()) {
            return new ActionResponse<>(HttpStatus.OK, List.of());
        }

        List<DistributionJobModel> distributionJobModels = jobAccessor.getJobsById(jobIdsToValidate);
        List<JobFieldModel> jobFieldModels = new LinkedList<>();
        for (DistributionJobModel distributionJobModel : distributionJobModels) {
            JobFieldModel jobFieldModel = JobFieldModelPopulationUtils.createJobFieldModelWithDefaultProviderProjectState(distributionJobModel);
            jobFieldModels.add(jobFieldModel);
        }
        return validateJobFieldModels(jobFieldModels);
    }

    private List<PermissionKey> gatherPermissionKeys() {
        List<PermissionKey> keys = new LinkedList<>();
        for (Descriptor descriptor : getDescriptorMap().getDescriptorMap().values()) {
            DescriptorKey descriptorKey = descriptor.getDescriptorKey();
            for (ConfigContextEnum context : ConfigContextEnum.values()) {
                if (descriptor.hasConfigForType(context)) {
                    keys.add(new PermissionKey(context.name(), descriptorKey.getUniversalKey()));
                }
            }
        }
        return keys;
    }

    private ActionResponse<List<JobFieldStatuses>> validateJobFieldModels(List<JobFieldModel> jobFieldModels) {
        List<JobFieldStatuses> errorsList = new LinkedList<>();
        for (JobFieldModel jobFieldModel : jobFieldModels) {
            List<AlertFieldStatus> fieldErrors = validateDescriptorFields(jobFieldModel);
            if (!fieldErrors.isEmpty()) {
                errorsList.add(new JobFieldStatuses(jobFieldModel.getJobId(), fieldErrors));
            }
        }
        return new ActionResponse<>(HttpStatus.OK, errorsList);
    }

    private List<AlertFieldStatus> validateDescriptorFields(JobFieldModel jobFieldModel) {
        List<AlertFieldStatus> fieldErrors;
        fieldErrors = jobFieldModel.getFieldModels()
            .stream()
            .map(FieldModel::getDescriptorName)
            .map(descriptorMap::getDescriptorKey)
            .flatMap(Optional::stream)
            .map(descriptorMap::getDescriptor)
            .flatMap(Optional::stream)
            .map(Descriptor::getDistributionValidator)
            .flatMap(Optional::stream)
            .map(validator -> validator.validate(jobFieldModel))
            .flatMap(Collection::stream)
            .collect(Collectors.toList());
        return fieldErrors;
    }

    @Override
    protected ValidationActionResponse testWithoutChecks(JobFieldModel resource) {
        ValidationResponseModel responseModel;
        String jobIdString = resource.getJobId();
        UUID jobId = Optional.ofNullable(jobIdString)
            .filter(StringUtils::isNotBlank)
            .map(UUID::fromString)
            .orElse(null);

        try {
            Collection<FieldModel> otherJobModels = new LinkedList<>();
            FieldModel channelFieldModel = getChannelFieldModelAndPopulateOtherJobModels(resource, otherJobModels);

            if (null != channelFieldModel) {
                String channelDescriptorName = channelFieldModel.getDescriptorName();

                Map<String, ConfigurationFieldModel> fields = createFieldsMap(channelFieldModel, otherJobModels);
                // The custom message fields are not written to the database or defined fields in the database.  Need to manually add them.
                // TODO Create a mechanism to create the field accessor with a combination of fields in the database and fields that are not.
                Optional<ConfigurationFieldModel> topicField = convertFieldToConfigurationField(channelFieldModel, FieldModelTestAction.KEY_CUSTOM_TOPIC);
                Optional<ConfigurationFieldModel> messageField = convertFieldToConfigurationField(channelFieldModel, FieldModelTestAction.KEY_CUSTOM_MESSAGE);
                topicField.ifPresent(model -> fields.put(FieldModelTestAction.KEY_CUSTOM_TOPIC, model));
                messageField.ifPresent(model -> fields.put(FieldModelTestAction.KEY_CUSTOM_MESSAGE, model));

                MessageResult providerTestResult = testProviderConfig(new FieldUtility(fields), jobIdString, channelFieldModel);
                if (providerTestResult.hasErrors()) {
                    responseModel = ValidationResponseModel.fromStatusCollection(providerTestResult.getStatusMessage(), providerTestResult.getFieldStatuses());
                    return new ValidationActionResponse(HttpStatus.OK, responseModel);
                }

                List<BlackDuckProjectDetailsModel> projectFilterDetails = Optional.ofNullable(resource.getConfiguredProviderProjects())
                    .orElse(List.of())
                    .stream()
                    .map(jobProject -> new BlackDuckProjectDetailsModel(jobProject.getName(), jobProject.getHref()))
                    .collect(Collectors.toList());

                DistributionJobModel testJobModel = distributionJobModelExtractor.convertToJobModel(jobId, fields, DateUtils.createCurrentDateTimestamp(), null, projectFilterDetails);
                DistributionChannelTestAction distributionChannelTestAction = channelTestActionMap.findRequiredAction(channelDescriptorName);
                MessageResult testActionResult = distributionChannelTestAction.testConfig(
                    testJobModel,
                    testJobModel.getName(),
                    topicField.flatMap(ConfigurationFieldModel::getFieldValue).orElse(null),
                    messageField.flatMap(ConfigurationFieldModel::getFieldValue).orElse(null)
                );
                List<AlertFieldStatus> resultFieldStatuses = testActionResult.getFieldStatuses();
                List<AlertFieldStatus> allStatuses = Stream.concat(resultFieldStatuses.stream(), providerTestResult.fieldWarnings().stream()).collect(Collectors.toList());
                responseModel = ValidationResponseModel.fromStatusCollection(testActionResult.getStatusMessage(), allStatuses);
                return new ValidationActionResponse(HttpStatus.OK, responseModel);
            }
            responseModel = ValidationResponseModel.generalError("No field model of type channel was was sent to test.");
            return new ValidationActionResponse(HttpStatus.BAD_REQUEST, responseModel);
        } catch (IntegrationRestException e) {
            logger.error(e.getMessage(), e);
            return ValidationActionResponse.createResponseFromIntegrationRestException(e);
        } catch (AlertFieldException e) {
            logger.error("Test Error with field Errors", e);
            responseModel = ValidationResponseModel.fromStatusCollection(e.getMessage(), e.getFieldErrors());
            return new ValidationActionResponse(HttpStatus.OK, responseModel);
        } catch (IntegrationException e) {
            // TODO this is not necessarily a PKIX
            responseModel = pkixErrorResponseFactory.createSSLExceptionResponse(e)
                .orElse(ValidationResponseModel.generalError(e.getMessage()));
            return new ValidationActionResponse(HttpStatus.OK, responseModel);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            responseModel = pkixErrorResponseFactory.createSSLExceptionResponse(e)
                .orElse(ValidationResponseModel.generalError(e.getMessage()));
            return new ValidationActionResponse(HttpStatus.OK, responseModel);
        }
    }

    private DistributionJobRequestModel createDistributionJobRequestModel(
        Collection<ConfigurationFieldModel> configFieldModels,
        List<JobProviderProjectFieldModel> jobProjects,
        OffsetDateTime createdAt,
        @Nullable OffsetDateTime lastUpdated
    ) throws AlertException {
        List<BlackDuckProjectDetailsModel> projectFilterDetails = jobProjects
            .stream()
            .map(jobProject -> new BlackDuckProjectDetailsModel(jobProject.getName(), jobProject.getHref()))
            .collect(Collectors.toList());
        Map<String, ConfigurationFieldModel> configuredFieldsMap = DataStructureUtils.mapToValues(configFieldModels, ConfigurationFieldModel::getFieldKey);
        DistributionJobModel fromResource = distributionJobModelExtractor.convertToJobModel(null, configuredFieldsMap, createdAt, lastUpdated, projectFilterDetails);

        return new DistributionJobRequestModel(
            fromResource.isEnabled(),
            fromResource.getName(),
            fromResource.getDistributionFrequency(),
            fromResource.getProcessingType(),
            fromResource.getChannelDescriptorName(),
            fromResource.getBlackDuckGlobalConfigId(),
            fromResource.isFilterByProject(),
            fromResource.getProjectNamePattern().orElse(null),
            fromResource.getProjectVersionNamePattern().orElse(null),
            fromResource.getNotificationTypes(),
            projectFilterDetails,
            fromResource.getPolicyFilterPolicyNames(),
            fromResource.getVulnerabilityFilterSeverityNames(),
            fromResource.getDistributionJobDetails()
        );
    }

    public ActionResponse<String> checkGlobalConfigExists(String descriptorName) {
        Optional<String> configMissingMessage = globalConfigExistsValidator.validate(descriptorName);
        if (configMissingMessage.isPresent()) {
            String message = configMissingMessage.get();
            return new ActionResponse<>(HttpStatus.BAD_REQUEST, message);
        }
        return new ActionResponse<>(HttpStatus.NO_CONTENT);
    }

    private FieldModel getChannelFieldModelAndPopulateOtherJobModels(JobFieldModel jobFieldModel, Collection<FieldModel> otherJobModels) throws AlertException {
        FieldModel channelFieldModel = null;
        for (FieldModel fieldModel : jobFieldModel.getFieldModels()) {
            Optional<Descriptor> descriptor = descriptorProcessor.retrieveDescriptor(fieldModel.getDescriptorName());
            FieldModel updatedFieldModel = fieldModelProcessor.createCustomMessageFieldModel(fieldModel);
            if (descriptor.filter(foundDescriptor -> DescriptorType.CHANNEL.equals(foundDescriptor.getType())).isPresent()) {
                channelFieldModel = updatedFieldModel;
            } else {
                otherJobModels.add(updatedFieldModel);
            }
        }
        return channelFieldModel;
    }

    private Map<String, ConfigurationFieldModel> createFieldsMap(FieldModel channelFieldModel, Collection<FieldModel> otherJobModels) throws AlertException {
        Map<String, ConfigurationFieldModel> fields = new HashMap<>();

        fields.putAll(modelConverter.convertToConfigurationFieldModelMap(channelFieldModel));
        Optional<ConfigurationModel> configurationFieldModel = configurationModelConfigurationAccessor.getConfigurationsByDescriptorNameAndContext(channelFieldModel.getDescriptorName(), ConfigContextEnum.GLOBAL).stream().findFirst();

        configurationFieldModel.ifPresent(model -> fields.putAll(model.getCopyOfKeyToFieldMap()));

        for (FieldModel fieldModel : otherJobModels) {
            fields.putAll(modelConverter.convertToConfigurationFieldModelMap(fieldModel));
        }
        return fields;
    }

    private MessageResult testProviderConfig(FieldUtility fieldUtility, String jobId, FieldModel fieldModel) throws IntegrationException {
        Optional<FieldModelTestAction> providerTestAction = fieldUtility.getString(ChannelDescriptor.KEY_PROVIDER_TYPE)
            .flatMap(providerName -> descriptorProcessor.retrieveTestAction(providerName, ConfigContextEnum.DISTRIBUTION));
        if (providerTestAction.isPresent()) {
            MessageResult providerConfigTestResult = providerTestAction.get().testConfig(jobId, fieldModel, fieldUtility);
            if (!providerConfigTestResult.hasErrors()) {
                return providerConfigTestResult;
            } else {
                List<AlertFieldStatus> deescalatedErrors = providerConfigTestResult.fieldErrors()
                    .stream()
                    .map(fieldStatus -> AlertFieldStatus.warning(fieldStatus.getFieldName(), fieldStatus.getFieldMessage()))
                    .collect(Collectors.toList());
                List<AlertFieldStatus> allWarnings = ListUtils.union(providerConfigTestResult.fieldWarnings(), deescalatedErrors);
                return new MessageResult("Provider Config Invalid", allWarnings);
            }
        }
        return new MessageResult("Provider Config Valid");
    }

    private Optional<ConfigurationFieldModel> convertFieldToConfigurationField(FieldModel fieldModel, String fieldKey) {
        Optional<FieldValueModel> fieldValueModel = fieldModel.getFieldValueModel(fieldKey);
        if (fieldValueModel.isPresent()) {
            ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(fieldKey);
            configurationFieldModel.setFieldValues(fieldValueModel.get().getValues());
            return Optional.of(configurationFieldModel);
        }
        return Optional.empty();
    }

}
