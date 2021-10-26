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
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.common.model.exception.AlertRuntimeException;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.FieldModelTestAction;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.channel.DistributionChannelTestAction;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.common.descriptor.config.GlobalConfigExistsValidator;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.persistence.model.job.DistributionJobModel;
import com.synopsys.integration.alert.common.persistence.model.job.details.MSTeamsJobDetailsModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
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
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class JobConfigActionsTest {
    private static final String DESCRIPTOR_KEY_STRING = "descriptorName";
    private static final String FIELD_VALUE = "fieldValue";
    private static final DescriptorType DESCRIPTOR_TYPE = DescriptorType.CHANNEL;

    private DescriptorKey descriptorKey;
    private Descriptor descriptor;
    private DescriptorMap descriptorMap;
    private UUID jobId;
    private FieldModel fieldModel;
    private JobFieldModel jobFieldModel;
    private DistributionJobModel distributionJobModel;
    private ConfigurationFieldModel configurationFieldModel;

    private AuthorizationManager mockedAuthorizationManager;
    private DescriptorAccessor mockedDescriptorAccessor;
    private ConfigurationModelConfigurationAccessor mockedConfigurationModelConfigurationAccessor;
    private JobAccessor mockedJobAccessor;
    private FieldModelProcessor mockedFieldModelProcessor;
    private DescriptorProcessor mockedDescriptorProcessor;
    private ConfigurationFieldModelConverter mockedConfigurationFieldModelConverter;
    private GlobalConfigExistsValidator mockedGlobalConfigExistsValidator;
    private PKIXErrorResponseFactory mockedPkixErrorResponseFactory;
    private DistributionJobModelExtractor mockedJobModelExtractor;

    private JobConfigActions defaultJobConfigActions;

    @BeforeEach
    public void init() {
        this.descriptorKey = new DescriptorKey(DESCRIPTOR_KEY_STRING, "Universal Key") {};
        this.descriptor = createDescriptor(Optional::empty, Optional::empty);
        this.descriptorMap = new DescriptorMap(List.of(descriptorKey), List.of(descriptor));
        this.fieldModel = createFieldModel();
        this.distributionJobModel = createDistributionJobModel();
        this.jobId = distributionJobModel.getJobId();
        this.jobFieldModel = new JobFieldModel(UUID.randomUUID().toString(), Set.of(fieldModel), List.of());
        this.configurationFieldModel = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        this.configurationFieldModel.setFieldValue(FIELD_VALUE);

        mockedAuthorizationManager = Mockito.mock(AuthorizationManager.class);
        mockedDescriptorAccessor = Mockito.mock(DescriptorAccessor.class);
        mockedConfigurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        mockedJobAccessor = Mockito.mock(JobAccessor.class);
        mockedFieldModelProcessor = Mockito.mock(FieldModelProcessor.class);
        mockedDescriptorProcessor = Mockito.mock(DescriptorProcessor.class);
        mockedConfigurationFieldModelConverter = Mockito.mock(ConfigurationFieldModelConverter.class);
        mockedGlobalConfigExistsValidator = Mockito.mock(GlobalConfigExistsValidator.class);
        mockedPkixErrorResponseFactory = Mockito.mock(PKIXErrorResponseFactory.class);

        mockedJobModelExtractor = Mockito.mock(DistributionJobModelExtractor.class);
        Mockito.when(mockedJobModelExtractor.convertToJobModel(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyList())).thenReturn(distributionJobModel);

        defaultJobConfigActions = createJobConfigActions(descriptorMap, List.of());

        Mockito.when(mockedAuthorizationManager.hasCreatePermission(Mockito.any(ConfigContextEnum.class), Mockito.eq(descriptorKey))).thenReturn(true);
        Mockito.when(mockedAuthorizationManager.hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.eq(descriptorKey))).thenReturn(true);
        Mockito.when(mockedAuthorizationManager.anyReadPermission(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(mockedAuthorizationManager.hasWritePermission(Mockito.any(ConfigContextEnum.class), Mockito.eq(descriptorKey))).thenReturn(true);
        Mockito.when(mockedAuthorizationManager.hasDeletePermission(Mockito.any(ConfigContextEnum.class), Mockito.eq(descriptorKey))).thenReturn(true);
        Mockito.when(mockedAuthorizationManager.hasExecutePermission(Mockito.any(ConfigContextEnum.class), Mockito.eq(descriptorKey))).thenReturn(true);

        Mockito.when(mockedAuthorizationManager.hasCreatePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(mockedAuthorizationManager.hasReadPermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(mockedAuthorizationManager.hasWritePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(mockedAuthorizationManager.hasDeletePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        Mockito.when(mockedAuthorizationManager.hasExecutePermission(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
    }

    @Test
    public void createTest() throws Exception {
        Mockito.when(mockedFieldModelProcessor.performBeforeSaveAction(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(mockedConfigurationFieldModelConverter.convertToConfigurationFieldModelMap(Mockito.any())).thenReturn(Map.of("Key", configurationFieldModel));
        Mockito.when(mockedConfigurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(mockedFieldModelProcessor.performAfterSaveAction(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(mockedJobAccessor.createJob(Mockito.any())).thenReturn(distributionJobModel);

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = defaultJobConfigActions.create(jobFieldModel);

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertTrue(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void createServerErrorTest() throws Exception {
        Mockito.doThrow(new AlertException("Exception for test")).when(mockedFieldModelProcessor).performBeforeSaveAction(Mockito.any());

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = defaultJobConfigActions.create(jobFieldModel);

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

        Mockito.when(mockedDescriptorAccessor.getRegisteredDescriptors()).thenReturn(List.of(registeredDescriptorModel));
        Mockito.when(mockedJobAccessor.getPageOfJobs(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyString(), Mockito.anyCollection())).thenReturn(pageOfJobs);
        Mockito.when(mockedConfigurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);

        ActionResponse<JobPagedModel> jobPagedModelActionResponse = defaultJobConfigActions.getPage(pageNumber, pageSize, "");

        assertTrue(jobPagedModelActionResponse.isSuccessful());
        assertTrue(jobPagedModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobPagedModelActionResponse.getHttpStatus());
    }

    @Test
    public void getOneTest() {
        Mockito.when(mockedJobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJobModel));

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = defaultJobConfigActions.getOne(jobId);

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertTrue(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void getOneErrorTest() throws Exception {
        Mockito.doThrow(new AlertException("Exception for Alert")).when(mockedFieldModelProcessor).performAfterReadAction(Mockito.any());

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = defaultJobConfigActions.getOne(jobId);

        assertTrue(jobFieldModelActionResponse.isError());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void updateTest() throws Exception {
        Mockito.when(mockedJobAccessor.getJobById(jobId)).thenReturn(Optional.of(distributionJobModel));
        Mockito.when(mockedJobAccessor.updateJob(Mockito.eq(distributionJobModel.getJobId()), Mockito.any())).thenReturn(distributionJobModel);
        Mockito.when(mockedFieldModelProcessor.performBeforeUpdateAction(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(mockedFieldModelProcessor.fillFieldModelWithExistingData(Mockito.anyLong(), Mockito.any())).thenReturn(List.of(configurationFieldModel));
        Mockito.when(mockedFieldModelProcessor.performAfterUpdateAction(Mockito.any(), Mockito.any())).thenReturn(fieldModel);

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = defaultJobConfigActions.update(jobId, jobFieldModel);

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertTrue(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void updateServerErrorTest() throws Exception {
        Mockito.when(mockedJobAccessor.getJobById(jobId)).thenReturn(Optional.of(distributionJobModel));
        Mockito.doThrow(new AlertConfigurationException("Exception for Alert test")).when(mockedFieldModelProcessor).performBeforeUpdateAction(Mockito.any());

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = defaultJobConfigActions.update(jobId, jobFieldModel);

        assertTrue(jobFieldModelActionResponse.isError());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void deleteTest() throws Exception {
        Mockito.when(mockedJobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJobModel));
        Mockito.when(mockedFieldModelProcessor.performBeforeDeleteAction(Mockito.any())).thenReturn(fieldModel);

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = defaultJobConfigActions.delete(jobId);

        Mockito.verify(mockedJobAccessor).deleteJob(Mockito.any());
        Mockito.verify(mockedFieldModelProcessor, Mockito.times(2)).performAfterDeleteAction(Mockito.any());

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void deleteServerErrorTest() throws Exception {
        Mockito.when(mockedJobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(distributionJobModel));
        Mockito.doThrow(new AlertException("Exception for Alert test")).when(mockedFieldModelProcessor).performBeforeDeleteAction(Mockito.any());

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = defaultJobConfigActions.delete(jobId);

        assertTrue(jobFieldModelActionResponse.isError());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void testTest() throws Exception {
        JobConfigActions jobConfigActionsForTest = new JobConfigActions(
            mockedAuthorizationManager,
            mockedDescriptorAccessor,
            mockedConfigurationModelConfigurationAccessor,
            mockedJobAccessor,
            mockedFieldModelProcessor,
            mockedDescriptorProcessor,
            mockedConfigurationFieldModelConverter,
            mockedGlobalConfigExistsValidator,
            mockedPkixErrorResponseFactory,
            descriptorMap,
            (id, list) -> {},
            List.of(createChannelDistributionTestAction()),
            mockedJobModelExtractor
        );

        fieldModel.setId("testID");

        Mockito.when(mockedDescriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));
        Mockito.when(mockedFieldModelProcessor.createCustomMessageFieldModel(Mockito.any())).thenReturn(fieldModel);

        ValidationActionResponse validationActionResponse = jobConfigActionsForTest.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful(), "Validation response was not successful");
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent(), "Missing content");
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent()
            .orElseThrow(() -> new AlertRuntimeException("Missing validation response"));
        assertFalse(validationResponseModel.hasErrors(), "Validation response had errors");
    }

    @Test
    public void testWithProviderWarningsTest() throws Exception {
        fieldModel.setId("testID");
        Mockito.when(mockedDescriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));
        Mockito.when(mockedFieldModelProcessor.createCustomMessageFieldModel(Mockito.any())).thenReturn(fieldModel);

        Mockito.when(mockedDescriptorProcessor.retrieveTestAction(Mockito.any())).thenReturn(Optional.of(createTestActionWithErrors()));
        Mockito.when(mockedConfigurationFieldModelConverter.convertToConfigurationFieldModelMap(Mockito.any())).thenReturn(Map.of(ChannelDescriptor.KEY_PROVIDER_TYPE, configurationFieldModel));
        Mockito.when(mockedDescriptorProcessor.retrieveTestAction(Mockito.any(), Mockito.any())).thenReturn(Optional.of(createTestActionWithErrors()));

        ValidationActionResponse validationActionResponse = defaultJobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful(), "Expected response to be successful");
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent(), "Expected response to have content");
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();

        assertTrue(validationResponseModel.hasErrors(), "Expected response to have error content");
    }

    @Test
    public void testBadRequestTest() {
        fieldModel.setId("testID");

        ValidationActionResponse validationActionResponse = defaultJobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isError());
        assertEquals(HttpStatus.BAD_REQUEST, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testAlertFieldExceptionTest() throws Exception {
        Mockito.when(mockedDescriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));

        AlertFieldStatus alertFieldStatus = AlertFieldStatus.error("fieldNameTest", "Alert Error Message");
        Mockito.doThrow(new AlertFieldException("AlertFieldException for Alert test", List.of(alertFieldStatus))).when(mockedFieldModelProcessor).createCustomMessageFieldModel(Mockito.any());

        ValidationActionResponse validationActionResponse = defaultJobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testIntegrationExceptionTest() throws Exception {
        Mockito.when(mockedDescriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));

        Mockito.doThrow(new AlertException("IntegrationException for Alert test")).when(mockedFieldModelProcessor).createCustomMessageFieldModel(Mockito.any());

        ValidationActionResponse validationActionResponse = defaultJobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testIntegrationRestExceptionTest() throws Exception {
        fieldModel.setId("testID");
        Mockito.when(mockedDescriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));
        Mockito.when(mockedFieldModelProcessor.createCustomMessageFieldModel(Mockito.any())).thenReturn(fieldModel);

        Mockito.when(mockedDescriptorProcessor.retrieveTestAction(Mockito.any())).thenReturn(Optional.of(createTestActionWithErrors()));
        Mockito.when(mockedConfigurationFieldModelConverter.convertToConfigurationFieldModelMap(Mockito.any())).thenReturn(Map.of(ChannelDescriptor.KEY_PROVIDER_TYPE, configurationFieldModel));
        Mockito.when(mockedDescriptorProcessor.retrieveTestAction(Mockito.any(), Mockito.any())).thenReturn(Optional.of(createTestActionWithIntegrationRestException()));

        ValidationActionResponse validationActionResponse = defaultJobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful(), "Expected response to be successful");
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testExceptionTest() throws Exception {
        Mockito.when(mockedDescriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));

        Mockito.doThrow(new NullPointerException("RuntimeException for Alert test")).when(mockedFieldModelProcessor).createCustomMessageFieldModel(Mockito.any());

        ValidationActionResponse validationActionResponse = defaultJobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void oldValidateTest() {

        ValidationActionResponse validationActionResponse = defaultJobConfigActions.validate(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    public void validateTest() {
        Descriptor descriptorWithValidator = createDescriptor(Optional::empty, () -> Optional.of(jobFieldModel -> Set.of()));
        JobConfigActions jobConfigActionsForTest = createJobConfigActions(new DescriptorMap(List.of(descriptorKey), List.of(descriptorWithValidator)), List.of());

        ValidationActionResponse validationActionResponse = jobConfigActionsForTest.validate(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    public void oldValidateBadRequestTest() {
        Mockito.when(mockedJobAccessor.getJobByName(Mockito.anyString())).thenReturn(Optional.of(distributionJobModel));

        ValidationActionResponse validationActionResponse = defaultJobConfigActions.validate(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors(), "Expected validation response to have errors");
    }

    @Test
    public void validateBadRequestTest() {
        Descriptor descriptorWithValidator = createDescriptor(Optional::empty, () -> Optional.of(jobFieldModel -> Set.of()));

        JobConfigActions jobConfigActionsForTest = createJobConfigActions(new DescriptorMap(List.of(descriptorKey), List.of(descriptorWithValidator)), List.of());

        Mockito.when(mockedJobAccessor.getJobByName(Mockito.anyString())).thenReturn(Optional.of(distributionJobModel));

        ValidationActionResponse validationActionResponse = jobConfigActionsForTest.validate(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors(), "Expected validation response to have errors");
    }

    @Test
    public void validateBadRequestWithFieldStatusTest() {
        AlertFieldStatus alertFieldStatus = AlertFieldStatus.error("fieldNameTest", "Alert Error Message");

        Descriptor mockDescriptor = Mockito.mock(Descriptor.class);
        Mockito.when(mockDescriptor.getDistributionValidator()).thenReturn(Optional.of(ignoredJobFieldModel -> Set.of(alertFieldStatus)));

        DescriptorMap mockDescriptorMap = Mockito.mock(DescriptorMap.class);
        Mockito.when(mockDescriptorMap.getDescriptorKey(Mockito.anyString())).thenReturn(Optional.of(descriptorKey));
        Mockito.when(mockDescriptorMap.getDescriptor(descriptorKey)).thenReturn(Optional.of(mockDescriptor));

        JobConfigActions testJobConfigActions = new JobConfigActions(mockedAuthorizationManager, null, null, mockedJobAccessor, null, null, null, null, null, mockDescriptorMap, null, List.of(), null);
        ValidationActionResponse validationActionResponse = testJobConfigActions.validate(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void validateJobsByIdTest() throws Exception {
        JobIdsRequestModel jobIdsRequestModel = new JobIdsRequestModel(List.of(jobId));
        Mockito.when(mockedAuthorizationManager.anyReadPermission(Mockito.any())).thenReturn(true);
        Mockito.when(mockedConfigurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(mockedFieldModelProcessor.performAfterReadAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);

        ActionResponse<List<JobFieldStatuses>> actionResponse = defaultJobConfigActions.validateJobsById(jobIdsRequestModel);

        assertTrue(actionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertTrue(actionResponse.hasContent());
    }

    @Test
    public void validateJobsByIdForbiddenTest() {
        JobIdsRequestModel jobIdsRequestModel = new JobIdsRequestModel(List.of(jobId));
        Mockito.when(mockedAuthorizationManager.anyReadPermission(Mockito.any())).thenReturn(false);

        ActionResponse<List<JobFieldStatuses>> actionResponse = defaultJobConfigActions.validateJobsById(jobIdsRequestModel);

        assertTrue(actionResponse.isError());
        assertEquals(HttpStatus.FORBIDDEN, actionResponse.getHttpStatus());
        assertFalse(actionResponse.hasContent());
    }

    @Test
    public void validateJobsByIdEmptyListTest() {
        JobIdsRequestModel jobIdsRequestModel = new JobIdsRequestModel(List.of());
        Mockito.when(mockedAuthorizationManager.anyReadPermission(Mockito.any())).thenReturn(true);

        ActionResponse<List<JobFieldStatuses>> actionResponse = defaultJobConfigActions.validateJobsById(jobIdsRequestModel);

        assertTrue(actionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertTrue(actionResponse.hasContent());
    }

    @Test
    public void checkGlobalConfigExistsTest() {
        Mockito.when(mockedGlobalConfigExistsValidator.validate(Mockito.any())).thenReturn(Optional.empty());

        ActionResponse<String> actionResponse = defaultJobConfigActions.checkGlobalConfigExists(DESCRIPTOR_KEY_STRING);

        assertTrue(actionResponse.isSuccessful());
        assertEquals(HttpStatus.NO_CONTENT, actionResponse.getHttpStatus());
        assertFalse(actionResponse.hasContent());
    }

    @Test
    public void checkGlobalConfigExistsBadRequestTest() {
        String configMissingMessage = "configMissingMessageTest";

        Mockito.when(mockedGlobalConfigExistsValidator.validate(Mockito.any())).thenReturn(Optional.of(configMissingMessage));

        ActionResponse<String> actionResponse = defaultJobConfigActions.checkGlobalConfigExists(DESCRIPTOR_KEY_STRING);

        assertTrue(actionResponse.isError());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
        assertFalse(actionResponse.hasContent());
    }

    private Descriptor createDescriptor(Supplier<Optional<GlobalConfigurationFieldModelValidator>> globalValidator, Supplier<Optional<DistributionConfigurationValidator>> distributionValidator) {
        Descriptor descriptor = new Descriptor(descriptorKey, DESCRIPTOR_TYPE, Set.of(ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION)) {
            @Override
            public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
                return globalValidator.get();
            }

            @Override
            public Optional<DistributionConfigurationValidator> getDistributionValidator() {
                return distributionValidator.get();
            }
        };

        return descriptor;
    }

    private DistributionJobModel createDistributionJobModel() {
        UUID jobId = UUID.randomUUID();
        return DistributionJobModel.builder()
            .jobId(jobId)
            .enabled(true)
            .name("A Job")
            .blackDuckGlobalConfigId(-1L)
            .distributionFrequency(FrequencyType.REAL_TIME)
            .processingType(ProcessingType.DEFAULT)
            .channelDescriptorName(DESCRIPTOR_KEY_STRING)
            .createdAt(OffsetDateTime.now())
            .filterByProject(false)
            .notificationTypes(List.of("notification_type"))
            .distributionJobDetails(new MSTeamsJobDetailsModel(jobId, "webhook"))
            .build();
    }

    private FieldModel createFieldModel() {
        String value = "testValue";
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(FieldModelTestAction.KEY_CUSTOM_TOPIC, new FieldValueModel(List.of(value), false));
        keyToValues.put(ChannelDescriptor.KEY_NAME, new FieldValueModel(List.of(value), false));
        return new FieldModel(descriptorKey.getUniversalKey(), ConfigContextEnum.DISTRIBUTION.name(), keyToValues);
    }

    private DistributionChannelTestAction createChannelDistributionTestAction() {
        return new DistributionChannelTestAction(descriptorKey) {
            @Override
            public MessageResult testConfig(DistributionJobModel distributionJobModel, String jobName, @Nullable String customTopic, @Nullable String customMessage) {
                return new MessageResult("Test Status Message");
            }
        };
    }

    private FieldModelTestAction createTestActionWithErrors() {
        FieldModelTestAction fieldModelTestAction = new FieldModelTestAction() {
            @Override
            public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
                AlertFieldStatus alertFieldStatus = AlertFieldStatus.error("fieldNameTest", "Alert Error Message");
                return new MessageResult("Test Status Message", List.of(alertFieldStatus));
            }
        };
        return fieldModelTestAction;
    }

    //Mockito can't throw an IntegrationRestException through a mock, we will need to create a mock instance that throws the specific exception
    private FieldModelTestAction createTestActionWithIntegrationRestException() {
        FieldModelTestAction fieldModelTestAction = new FieldModelTestAction() {
            @Override
            public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
                throw new IntegrationRestException(HttpMethod.GET, new HttpUrl("https://google.com"), HttpStatus.OK.value(), "httpStatusMessageTest", "httpResponseContentTest", "IntegrationRestExceptionForAlertTest");
            }
        };
        return fieldModelTestAction;
    }

    private JobConfigActions createJobConfigActions(DescriptorMap customDescriptorMap, List<DistributionChannelTestAction> customDistributionChannelTestActions) {
        return new JobConfigActions(
            mockedAuthorizationManager,
            mockedDescriptorAccessor,
            mockedConfigurationModelConfigurationAccessor,
            mockedJobAccessor,
            mockedFieldModelProcessor,
            mockedDescriptorProcessor,
            mockedConfigurationFieldModelConverter,
            mockedGlobalConfigExistsValidator,
            mockedPkixErrorResponseFactory,
            customDescriptorMap,
            (id, list) -> {},
            customDistributionChannelTestActions,
            mockedJobModelExtractor
        );
    }

}
