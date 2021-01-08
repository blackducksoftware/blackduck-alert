package com.synopsys.integration.alert.web.api.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.TestAction;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.channel.ChannelDistributionTestAction;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.GlobalConfigExistsValidator;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.UIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertMethodNotAllowedException;
import com.synopsys.integration.alert.common.exception.AlertRuntimeException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.DistributionJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.processor.JobDetailsProcessor;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.provider.ProviderProjectExistencePopulator;
import com.synopsys.integration.alert.common.rest.FieldModelProcessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldStatuses;
import com.synopsys.integration.alert.common.rest.model.JobIdsRequestModel;
import com.synopsys.integration.alert.common.rest.model.JobPagedModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.certificates.web.PKIXErrorResponseFactory;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class JobConfigActionsTest {
    private static final String DESCRIPTOR_NAME = "descriptorName";
    private static final String FIELD_VALUE = "fieldValue";
    private static final DescriptorType DESCRIPTOR_TYPE = DescriptorType.CHANNEL;

    private UUID jobId;
    private FieldModel fieldModel;
    private JobFieldModel jobFieldModel;
    private DistributionJobModel distributionJobModel;
    private ConfigurationFieldModel configurationFieldModel;

    private AuthorizationManager authorizationManager;
    private DescriptorAccessor descriptorAccessor;
    private ConfigurationAccessor configurationAccessor;
    private JobAccessor jobAccessor;
    private FieldModelProcessor fieldModelProcessor;
    private DescriptorProcessor descriptorProcessor;
    private ConfigurationFieldModelConverter configurationFieldModelConverter;
    private GlobalConfigExistsValidator globalConfigExistsValidator;
    private PKIXErrorResponseFactory pkixErrorResponseFactory;
    private DescriptorMap descriptorMap;

    private JobConfigActions jobConfigActions;

    @BeforeEach
    public void init() {
        this.fieldModel = createFieldModel();
        this.distributionJobModel = createMockDistributionJobModel();
        this.jobId = distributionJobModel.getJobId();
        this.jobFieldModel = new JobFieldModel(UUID.randomUUID().toString(), Set.of(fieldModel), List.of());
        this.configurationFieldModel = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        this.configurationFieldModel.setFieldValue(FIELD_VALUE);

        ProviderProjectExistencePopulator providerProjectExistencePopulator = (id, list) -> {};

        authorizationManager = Mockito.mock(AuthorizationManager.class);
        descriptorAccessor = Mockito.mock(DescriptorAccessor.class);
        configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        jobAccessor = Mockito.mock(JobAccessor.class);
        fieldModelProcessor = Mockito.mock(FieldModelProcessor.class);
        descriptorProcessor = Mockito.mock(DescriptorProcessor.class);
        configurationFieldModelConverter = Mockito.mock(ConfigurationFieldModelConverter.class);
        globalConfigExistsValidator = Mockito.mock(GlobalConfigExistsValidator.class);
        pkixErrorResponseFactory = Mockito.mock(PKIXErrorResponseFactory.class);
        descriptorMap = Mockito.mock(DescriptorMap.class);
        jobConfigActions = new JobConfigActions(
            authorizationManager,
            descriptorAccessor,
            configurationAccessor,
            jobAccessor,
            fieldModelProcessor,
            descriptorProcessor,
            configurationFieldModelConverter,
            globalConfigExistsValidator,
            pkixErrorResponseFactory,
            descriptorMap,
            providerProjectExistencePopulator
        );

        Mockito.when(authorizationManager.hasCreatePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);
        Mockito.when(authorizationManager.anyReadPermission(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(authorizationManager.hasWritePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);
        Mockito.when(authorizationManager.hasDeletePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class))).thenReturn(true);

        Mockito.when(authorizationManager.hasCreatePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(authorizationManager.hasWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(authorizationManager.hasDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    }

    @Test
    public void createTest() throws Exception {
        Mockito.when(fieldModelProcessor.performBeforeSaveAction(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(configurationFieldModelConverter.convertToConfigurationFieldModelMap(Mockito.any())).thenReturn(Map.of("Key", configurationFieldModel));
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.performAfterSaveAction(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(jobAccessor.createJob(Mockito.any())).thenReturn(distributionJobModel);
        Mockito.when(descriptorProcessor.retrieveJobDetailsProcessor(Mockito.anyString())).thenReturn(Optional.of(createJobDetailsProcessor()));

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.create(jobFieldModel);

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertTrue(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void createServerErrorTest() throws Exception {
        Mockito.doThrow(new AlertException("Exception for test")).when(fieldModelProcessor).performBeforeSaveAction(Mockito.any());

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.create(jobFieldModel);

        assertTrue(jobFieldModelActionResponse.isError());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void getPageTest() throws Exception {
        int totalPages = 1;
        int pageNumber = 0;
        int pageSize = 10;
        RegisteredDescriptorModel registeredDescriptorModel = new RegisteredDescriptorModel(1L, "descriptorName", DESCRIPTOR_TYPE.name());
        AlertPagedModel<DistributionJobModel> pageOfJobs = new AlertPagedModel<>(totalPages, pageNumber, pageSize, List.of(distributionJobModel));

        Mockito.when(descriptorAccessor.getRegisteredDescriptors()).thenReturn(List.of(registeredDescriptorModel));
        Mockito.when(jobAccessor.getPageOfJobs(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyCollection())).thenReturn(pageOfJobs);
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);

        ActionResponse<JobPagedModel> jobPagedModelActionResponse = jobConfigActions.getPage(pageNumber, pageSize, "");

        assertTrue(jobPagedModelActionResponse.isSuccessful());
        assertTrue(jobPagedModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobPagedModelActionResponse.getHttpStatus());
    }

    @Test
    public void getOneTest() {
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJobModel));

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.getOne(jobId);

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertTrue(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void getOneErrorTest() throws Exception {
        Mockito.doThrow(new AlertException("Exception for Alert")).when(fieldModelProcessor).performAfterReadAction(Mockito.any());

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.getOne(jobId);

        assertTrue(jobFieldModelActionResponse.isError());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void updateTest() throws Exception {
        Mockito.when(jobAccessor.getJobById(jobId)).thenReturn(Optional.of(distributionJobModel));
        Mockito.when(jobAccessor.updateJob(Mockito.eq(distributionJobModel.getJobId()), Mockito.any())).thenReturn(distributionJobModel);
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(fieldModelProcessor.performBeforeUpdateAction(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.fillFieldModelWithExistingData(Mockito.anyLong(), Mockito.any())).thenReturn(List.of(configurationFieldModel));
        Mockito.when(fieldModelProcessor.performAfterUpdateAction(Mockito.any(), Mockito.any())).thenReturn(fieldModel);
        Mockito.when(descriptorProcessor.retrieveJobDetailsProcessor(Mockito.anyString())).thenReturn(Optional.of(createJobDetailsProcessor()));

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.update(jobId, jobFieldModel);

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertTrue(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void updateServerErrorTest() throws Exception {
        Mockito.when(jobAccessor.getJobById(jobId)).thenReturn(Optional.of(distributionJobModel));
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.doThrow(new AlertConfigurationException("Exception for Alert test")).when(fieldModelProcessor).performBeforeUpdateAction(Mockito.any());

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.update(jobId, jobFieldModel);

        assertTrue(jobFieldModelActionResponse.isError());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void deleteTest() throws Exception {
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJobModel));
        Mockito.when(fieldModelProcessor.performBeforeDeleteAction(Mockito.any())).thenReturn(fieldModel);

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.delete(jobId);

        Mockito.verify(jobAccessor).deleteJob(Mockito.any());
        Mockito.verify(fieldModelProcessor, Mockito.times(2)).performAfterDeleteAction(Mockito.any());

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void deleteServerErrorTest() throws Exception {
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJobModel));
        Mockito.doThrow(new AlertException("Exception for Alert test")).when(fieldModelProcessor).performBeforeDeleteAction(Mockito.any());

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.delete(jobId);

        assertTrue(jobFieldModelActionResponse.isError());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void testTest() throws Exception {
        fieldModel.setId("testID");
        Descriptor descriptor = createDescriptor(DescriptorType.CHANNEL);

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(descriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));
        Mockito.when(fieldModelProcessor.createCustomMessageFieldModel(Mockito.any())).thenReturn(fieldModel);

        Mockito.when(descriptorProcessor.retrieveChannelDistributionTestAction(Mockito.any())).thenReturn(Optional.of(createChannelDistributionTestAction()));
        Mockito.when(descriptorProcessor.retrieveJobDetailsProcessor(Mockito.anyString())).thenReturn(Optional.of(createJobDetailsProcessor()));
        Mockito.when(configurationFieldModelConverter.convertToConfigurationFieldModelMap(Mockito.any())).thenReturn(Map.of("testKey", configurationFieldModel));

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful(), "Validation response was not successful");
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent(), "Missing content");
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent()
                                                              .orElseThrow(() -> new AlertRuntimeException("Missing validation response"));
        assertFalse(validationResponseModel.hasErrors(), "Validation response had errors");
    }

    @Test
    public void testWithProviderErrorsTest() throws Exception {
        fieldModel.setId("testID");
        Descriptor descriptor = createDescriptor(DescriptorType.CHANNEL);

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(descriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));
        Mockito.when(fieldModelProcessor.createCustomMessageFieldModel(Mockito.any())).thenReturn(fieldModel);

        Mockito.when(descriptorProcessor.retrieveTestAction(Mockito.any())).thenReturn(Optional.of(createTestActionWithErrors()));
        Mockito.when(descriptorProcessor.retrieveChannelDistributionTestAction(Mockito.any())).thenReturn(Optional.of(createChannelDistributionTestAction()));
        Mockito.when(configurationFieldModelConverter.convertToConfigurationFieldModelMap(Mockito.any())).thenReturn(Map.of(ChannelDistributionUIConfig.KEY_PROVIDER_NAME, configurationFieldModel));
        Mockito.when(descriptorProcessor.retrieveTestAction(Mockito.any(), Mockito.any())).thenReturn(Optional.of(createTestActionWithErrors()));

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful(), "Expected response to be successful");
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent(), "Expected response to have content");
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors(), "Expected response to ");
    }

    @Test
    public void testMethodNotAllowedTest() throws Exception {
        fieldModel.setId("testID");
        Descriptor descriptor = createDescriptor(DescriptorType.CHANNEL);

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(descriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));
        Mockito.when(fieldModelProcessor.createCustomMessageFieldModel(Mockito.any())).thenReturn(fieldModel);

        Mockito.when(descriptorProcessor.retrieveTestAction(Mockito.any())).thenReturn(Optional.empty());

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isError());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testBadRequestTest() {
        fieldModel.setId("testID");
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isError());
        assertEquals(HttpStatus.BAD_REQUEST, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testAlertFieldExceptionTest() throws Exception {
        Descriptor descriptor = createDescriptor(DescriptorType.CHANNEL);

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(descriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));

        AlertFieldStatus alertFieldStatus = AlertFieldStatus.error("fieldNameTest", "Alert Error Message");
        Mockito.doThrow(new AlertFieldException("AlertFieldException for Alert test", List.of(alertFieldStatus))).when(fieldModelProcessor).createCustomMessageFieldModel(Mockito.any());

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testAlertMethodNotAllowedTest() throws Exception {
        Descriptor descriptor = createDescriptor(DescriptorType.CHANNEL);

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(descriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));

        Mockito.doThrow(new AlertMethodNotAllowedException("AlertMethodNotAllowedException for Alert test")).when(fieldModelProcessor).createCustomMessageFieldModel(Mockito.any());

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isError());
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testIntegrationExceptionTest() throws Exception {
        Descriptor descriptor = createDescriptor(DescriptorType.CHANNEL);

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(descriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));

        Mockito.doThrow(new AlertException("IntegrationException for Alert test")).when(fieldModelProcessor).createCustomMessageFieldModel(Mockito.any());

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testIntegrationRestExceptionTest() throws Exception {
        fieldModel.setId("testID");
        Descriptor descriptor = createDescriptor(DescriptorType.CHANNEL);

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(descriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));
        Mockito.when(fieldModelProcessor.createCustomMessageFieldModel(Mockito.any())).thenReturn(fieldModel);

        Mockito.when(descriptorProcessor.retrieveTestAction(Mockito.any())).thenReturn(Optional.of(createTestActionWithErrors()));
        Mockito.when(descriptorProcessor.retrieveChannelDistributionTestAction(Mockito.any())).thenReturn(Optional.of(createChannelDistributionTestAction()));
        Mockito.when(configurationFieldModelConverter.convertToConfigurationFieldModelMap(Mockito.any())).thenReturn(Map.of(ChannelDistributionUIConfig.KEY_PROVIDER_NAME, configurationFieldModel));
        Mockito.when(descriptorProcessor.retrieveTestAction(Mockito.any(), Mockito.any())).thenReturn(Optional.of(createTestActionWithIntegrationRestException()));

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful(), "Expected response to be successful");
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testExceptionTest() throws Exception {
        Descriptor descriptor = createDescriptor(DescriptorType.CHANNEL);

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(descriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));

        Mockito.doThrow(new NullPointerException("RuntimeException for Alert test")).when(fieldModelProcessor).createCustomMessageFieldModel(Mockito.any());

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void validateTest() {
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());

        ValidationActionResponse validationActionResponse = jobConfigActions.validate(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    public void validateBadRequestTest() {
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(jobAccessor.getJobByName(Mockito.anyString())).thenReturn(Optional.of(distributionJobModel));

        ValidationActionResponse validationActionResponse = jobConfigActions.validate(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors(), "Expected validation response to have errors");
    }

    @Test
    public void validateBadRequestWithFieldStatusTest() {
        AlertFieldStatus alertFieldStatus = AlertFieldStatus.error("fieldNameTest", "Alert Error Message");
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of(alertFieldStatus));

        ValidationActionResponse validationActionResponse = jobConfigActions.validate(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void validateJobsByIdTest() throws Exception {
        JobIdsRequestModel jobIdsRequestModel = new JobIdsRequestModel(List.of(jobId));
        DescriptorKey descriptorKey = createDescriptorKey();
        Descriptor descriptor = createDescriptor(DESCRIPTOR_TYPE);

        Mockito.when(descriptorMap.getDescriptorMap()).thenReturn(Map.of(descriptorKey, descriptor));
        Mockito.when(authorizationManager.anyReadPermission(Mockito.any())).thenReturn(true);
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.performAfterReadAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);

        AlertFieldStatus alertFieldStatus = AlertFieldStatus.error("fieldNameTest", "Alert Error Message");
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of(alertFieldStatus));

        ActionResponse<List<JobFieldStatuses>> actionResponse = jobConfigActions.validateJobsById(jobIdsRequestModel);

        assertTrue(actionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertTrue(actionResponse.hasContent());
    }

    @Test
    public void validateJobsByIdForbiddenTest() {
        JobIdsRequestModel jobIdsRequestModel = new JobIdsRequestModel(List.of(jobId));
        DescriptorKey descriptorKey = createDescriptorKey();
        Descriptor descriptor = createDescriptor(DESCRIPTOR_TYPE);

        Mockito.when(descriptorMap.getDescriptorMap()).thenReturn(Map.of(descriptorKey, descriptor));
        Mockito.when(authorizationManager.anyReadPermission(Mockito.any())).thenReturn(false);

        ActionResponse<List<JobFieldStatuses>> actionResponse = jobConfigActions.validateJobsById(jobIdsRequestModel);

        assertTrue(actionResponse.isError());
        assertEquals(HttpStatus.FORBIDDEN, actionResponse.getHttpStatus());
        assertFalse(actionResponse.hasContent());
    }

    @Test
    public void validateJobsByIdEmptyListTest() {
        JobIdsRequestModel jobIdsRequestModel = new JobIdsRequestModel(List.of());
        DescriptorKey descriptorKey = createDescriptorKey();
        Descriptor descriptor = createDescriptor(DESCRIPTOR_TYPE);

        Mockito.when(descriptorMap.getDescriptorMap()).thenReturn(Map.of(descriptorKey, descriptor));
        Mockito.when(authorizationManager.anyReadPermission(Mockito.any())).thenReturn(true);

        AlertFieldStatus alertFieldStatus = AlertFieldStatus.error("fieldNameTest", "Alert Error Message");
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of(alertFieldStatus));

        ActionResponse<List<JobFieldStatuses>> actionResponse = jobConfigActions.validateJobsById(jobIdsRequestModel);

        assertTrue(actionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertTrue(actionResponse.hasContent());
    }

    @Test
    public void checkGlobalConfigExistsTest() {
        Mockito.when(globalConfigExistsValidator.validate(Mockito.any())).thenReturn(Optional.empty());

        ActionResponse<String> actionResponse = jobConfigActions.checkGlobalConfigExists(DESCRIPTOR_NAME);

        assertTrue(actionResponse.isSuccessful());
        assertEquals(HttpStatus.NO_CONTENT, actionResponse.getHttpStatus());
        assertFalse(actionResponse.hasContent());
    }

    @Test
    public void checkGlobalConfigExistsBadRequestTest() {
        String configMissingMessage = "configMissingMessageTest";

        Mockito.when(globalConfigExistsValidator.validate(Mockito.any())).thenReturn(Optional.of(configMissingMessage));

        ActionResponse<String> actionResponse = jobConfigActions.checkGlobalConfigExists(DESCRIPTOR_NAME);

        assertTrue(actionResponse.isError());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
        assertFalse(actionResponse.hasContent());
    }

    private DistributionJobModel createMockDistributionJobModel() {
        return DistributionJobModel.builder()
                   .jobId(UUID.randomUUID())
                   .enabled(true)
                   .name("A Job")
                   .blackDuckGlobalConfigId(-1L)
                   .distributionFrequency(FrequencyType.REAL_TIME)
                   .processingType(ProcessingType.DEFAULT)
                   .channelDescriptorName(DESCRIPTOR_NAME)
                   .createdAt(OffsetDateTime.now())
                   .filterByProject(false)
                   .notificationTypes(List.of("notification_type"))
                   .distributionJobDetails(new MSTeamsJobDetailsModel("webhook"))
                   .build();
    }

    private FieldModel createFieldModel() {
        String value = "testValue";
        DescriptorKey descriptorKey = createDescriptorKey();
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(TestAction.KEY_CUSTOM_TOPIC, new FieldValueModel(List.of(value), false));
        keyToValues.put(ChannelDistributionUIConfig.KEY_NAME, new FieldValueModel(List.of(value), false));
        return new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.DISTRIBUTION.name(), keyToValues);
    }

    private ConfigurationModel createConfigurationModel() {
        Long descriptorId = 1L;
        Long configurationId = 2L;
        String createdAt = "createdAt-test";
        String lastUpdated = "lastUpdated-test";
        ConfigContextEnum configContextEnum = ConfigContextEnum.GLOBAL;
        String fieldKey = "fieldKey";

        Map<String, ConfigurationFieldModel> configuredFields = Map.of(fieldKey, configurationFieldModel);
        return new ConfigurationModel(descriptorId, configurationId, createdAt, lastUpdated, configContextEnum, configuredFields);
    }

    private ChannelDistributionTestAction createChannelDistributionTestAction() {
        return new ChannelDistributionTestAction(null) {
            @Override
            public MessageResult testConfig(DistributionJobModel testJobModel, ConfigurationModel channelGlobalConfig, String customTopic, String customMessage, String destination) {
                return new MessageResult("Test Status Message");
            }
        };
    }

    private JobDetailsProcessor createJobDetailsProcessor() {
        return new JobDetailsProcessor() {
            @Override
            protected DistributionJobDetailsModel convertToChannelJobDetails(Map<String, ConfigurationFieldModel> configuredFieldsMap) {
                return new DistributionJobDetailsModel(createChannelKey()) {};
            }
        };
    }

    private TestAction createTestActionWithErrors() {
        TestAction testAction = new TestAction() {
            @Override
            public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
                AlertFieldStatus alertFieldStatus = AlertFieldStatus.error("fieldNameTest", "Alert Error Message");
                return new MessageResult("Test Status Message", List.of(alertFieldStatus));
            }
        };
        return testAction;
    }

    //Mockito can't throw an IntegrationRestException through a mock, we will need to create a mock instance that throws the specific exception
    private TestAction createTestActionWithIntegrationRestException() {
        TestAction testAction = new TestAction() {
            @Override
            public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
                throw new IntegrationRestException(HttpStatus.OK.value(), "httpStatusMessageTest", "httpResponseContentTest", "IntegrationRestExceptionForAlertTest");
            }
        };
        return testAction;
    }

    private DescriptorKey createDescriptorKey() {
        return new DescriptorKey("universal_key", "Universal Key") {};
    }

    private ChannelKey createChannelKey() {
        return new ChannelKey("channel_key", "Channel Key");
    }

    private Descriptor createDescriptor(DescriptorType descriptorType) {
        DescriptorKey descriptorKey = createDescriptorKey();
        Descriptor descriptor = new Descriptor(descriptorKey, descriptorType) {
            @Override
            public DescriptorKey getDescriptorKey() {
                return descriptorKey;
            }
        };
        UIConfig uiConfig = new UIConfig("label", "description", "url", "componentNamespace") {
            @Override
            protected List<ConfigField> createFields() {
                return List.of();
            }
        };
        descriptor.addGlobalUiConfig(uiConfig);
        return descriptor;
    }

}
