package com.synopsys.integration.alert.web.api.job;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
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
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.common.exception.AlertMethodNotAllowedException;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.accessor.JobAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.RegisteredDescriptorModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.FieldModelProcessor;
import com.synopsys.integration.alert.common.rest.model.AlertPagedModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldStatuses;
import com.synopsys.integration.alert.common.rest.model.JobIdsValidationRequestModel;
import com.synopsys.integration.alert.common.rest.model.JobPagedModel;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.certificates.web.PKIXErrorResponseFactory;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.exception.IntegrationRestException;

public class JobConfigActionsTest {
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

        jobConfigActions = new JobConfigActions(authorizationManager, descriptorAccessor, configurationAccessor, jobAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, globalConfigExistsValidator,
            pkixErrorResponseFactory, descriptorMap);

        Mockito.when(authorizationManager.hasCreatePermission(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(authorizationManager.hasReadPermission(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(authorizationManager.anyReadPermission(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(authorizationManager.hasWritePermission(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(authorizationManager.hasDeletePermission(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(authorizationManager.hasExecutePermission(Mockito.any(), Mockito.any())).thenReturn(true);
    }

    @Test
    public void createTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));

        Mockito.when(fieldModelProcessor.performBeforeSaveAction(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(configurationFieldModelConverter.convertToConfigurationFieldModelMap(Mockito.any())).thenReturn(Map.of("Key", configurationFieldModel));
        Mockito.when(jobAccessor.createJob(Mockito.anyCollection(), Mockito.anyCollection())).thenReturn(configurationJobModel);
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.performAfterSaveAction(Mockito.any())).thenReturn(fieldModel);

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.create(jobFieldModel);

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertTrue(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void createServerErrorTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));

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
        DescriptorType descriptorType = DescriptorType.CHANNEL;
        RegisteredDescriptorModel registeredDescriptorModel = new RegisteredDescriptorModel(1L, "descriptorName", descriptorType.name());
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));
        AlertPagedModel<ConfigurationJobModel> pageOfJobs = new AlertPagedModel(totalPages, pageNumber, pageSize, List.of(configurationJobModel));

        Mockito.when(descriptorAccessor.getRegisteredDescriptors()).thenReturn(List.of(registeredDescriptorModel));
        Mockito.when(jobAccessor.getPageOfJobs(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyCollection())).thenReturn(pageOfJobs);
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);

        ActionResponse<JobPagedModel> jobPagedModelActionResponse = jobConfigActions.getPage(pageNumber, pageSize);

        assertTrue(jobPagedModelActionResponse.isSuccessful());
        assertTrue(jobPagedModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobPagedModelActionResponse.getHttpStatus());
    }

    @Test
    public void getPageServerErrorTest() throws Exception {
        int totalPages = 1;
        int pageNumber = 0;
        int pageSize = 10;
        final DescriptorType descriptorType = DescriptorType.CHANNEL;
        RegisteredDescriptorModel registeredDescriptorModel = new RegisteredDescriptorModel(1L, "descriptorName", descriptorType.name());
        UUID jobId = UUID.randomUUID();
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));
        AlertPagedModel<ConfigurationJobModel> pageOfJobs = new AlertPagedModel(totalPages, pageNumber, pageSize, List.of(configurationJobModel));

        Mockito.when(descriptorAccessor.getRegisteredDescriptors()).thenReturn(List.of(registeredDescriptorModel));
        Mockito.when(jobAccessor.getPageOfJobs(Mockito.anyInt(), Mockito.anyInt(), Mockito.anyCollection())).thenReturn(pageOfJobs);
        Mockito.doThrow(new AlertDatabaseConstraintException("Exception for Alert tests")).when(configurationFieldModelConverter).convertToFieldModel(Mockito.any());

        ActionResponse<JobPagedModel> jobPagedModelActionResponse = jobConfigActions.getPage(pageNumber, pageSize);

        assertTrue(jobPagedModelActionResponse.isError());
        assertFalse(jobPagedModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, jobPagedModelActionResponse.getHttpStatus());
    }

    @Test
    public void getOneTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));

        mockFindJobFieldModel(configurationJobModel, fieldModel);

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.getOne(jobId);

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertTrue(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void getOneErrorTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));

        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(configurationJobModel));
        Mockito.doThrow(new AlertDatabaseConstraintException("Exception for Alert")).when(configurationFieldModelConverter).convertToFieldModel(Mockito.any());

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.getOne(jobId);

        assertTrue(jobFieldModelActionResponse.isError());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.NOT_FOUND, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void updateTest() throws Exception {
        String fieldValue = "fieldValue";

        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        configurationFieldModel.setFieldValue(fieldValue);

        mockFindJobFieldModel(configurationJobModel, fieldModel);
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(fieldModelProcessor.performBeforeUpdateAction(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.fillFieldModelWithExistingData(Mockito.anyLong(), Mockito.any())).thenReturn(List.of(configurationFieldModel));
        Mockito.when(jobAccessor.updateJob(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(configurationJobModel);
        Mockito.when(fieldModelProcessor.performAfterUpdateAction(Mockito.any(), Mockito.any())).thenReturn(fieldModel);

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.update(jobId, jobFieldModel);

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertTrue(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void updateServerErrorTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));

        mockFindJobFieldModel(configurationJobModel, fieldModel);
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.doThrow(new AlertDatabaseConstraintException("Exception for Alert test")).when(fieldModelProcessor).performBeforeUpdateAction(Mockito.any());

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.update(jobId, jobFieldModel);

        assertTrue(jobFieldModelActionResponse.isError());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void deleteTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));

        mockFindJobFieldModel(configurationJobModel, fieldModel);
        Mockito.when(fieldModelProcessor.performBeforeDeleteAction(Mockito.any())).thenReturn(fieldModel);

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.delete(jobId);

        Mockito.verify(jobAccessor).deleteJob(Mockito.any());
        Mockito.verify(fieldModelProcessor).performAfterDeleteAction(Mockito.any());

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void deleteServerErrorTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));

        mockFindJobFieldModel(configurationJobModel, fieldModel);
        Mockito.doThrow(new AlertException("Exception for Alert test")).when(fieldModelProcessor).performBeforeDeleteAction(Mockito.any());

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.delete(jobId);

        assertTrue(jobFieldModelActionResponse.isError());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void testTest() throws Exception {
        String fieldValue = "fieldValue";
        DescriptorType descriptorType = DescriptorType.CHANNEL;
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        fieldModel.setId("testID");
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));

        Descriptor descriptor = Mockito.mock(Descriptor.class);

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(descriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));
        Mockito.when(fieldModelProcessor.createCustomMessageFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(descriptor.getType()).thenReturn(descriptorType);

        Mockito.when(descriptorProcessor.retrieveTestAction(Mockito.any())).thenReturn(Optional.of(createTestAction()));
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        configurationFieldModel.setFieldValue(fieldValue);
        Mockito.when(configurationFieldModelConverter.convertToConfigurationFieldModelMap(Mockito.any())).thenReturn(Map.of("testKey", configurationFieldModel));

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertFalse(validationResponseModel.hasErrors());
    }

    @Test
    public void testWithProviderErrorsTest() throws Exception {
        String fieldValue = "fieldValue";
        DescriptorType descriptorType = DescriptorType.CHANNEL;
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        fieldModel.setId("testID");
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));

        Descriptor descriptor = Mockito.mock(Descriptor.class);

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(descriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));
        Mockito.when(fieldModelProcessor.createCustomMessageFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(descriptor.getType()).thenReturn(descriptorType);

        Mockito.when(descriptorProcessor.retrieveTestAction(Mockito.any())).thenReturn(Optional.of(createTestActionWithErrors()));
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        configurationFieldModel.setFieldValue(fieldValue);
        Mockito.when(configurationFieldModelConverter.convertToConfigurationFieldModelMap(Mockito.any())).thenReturn(Map.of(ChannelDistributionUIConfig.KEY_PROVIDER_NAME, configurationFieldModel));
        Mockito.when(descriptorProcessor.retrieveTestAction(Mockito.any(), Mockito.any())).thenReturn(Optional.of(createTestActionWithErrors()));

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testMethodNotAllowedTest() throws Exception {
        DescriptorType descriptorType = DescriptorType.CHANNEL;
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        fieldModel.setId("testID");
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));

        Descriptor descriptor = Mockito.mock(Descriptor.class);

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(descriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));
        Mockito.when(fieldModelProcessor.createCustomMessageFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(descriptor.getType()).thenReturn(descriptorType);

        Mockito.when(descriptorProcessor.retrieveTestAction(Mockito.any())).thenReturn(Optional.empty());

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testBadRequestTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        fieldModel.setId("testID");
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testAlertFieldExceptionTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));

        Descriptor descriptor = Mockito.mock(Descriptor.class);

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
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));

        Descriptor descriptor = Mockito.mock(Descriptor.class);

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(descriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));

        Mockito.doThrow(new AlertMethodNotAllowedException("AlertMethodNotAllowedException for Alert test")).when(fieldModelProcessor).createCustomMessageFieldModel(Mockito.any());

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testIntegrationExceptionTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));

        Descriptor descriptor = Mockito.mock(Descriptor.class);

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
        String fieldValue = "fieldValue";
        DescriptorType descriptorType = DescriptorType.CHANNEL;
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        fieldModel.setId("testID");
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));

        Descriptor descriptor = Mockito.mock(Descriptor.class);

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(descriptorProcessor.retrieveDescriptor(Mockito.any())).thenReturn(Optional.of(descriptor));
        Mockito.when(fieldModelProcessor.createCustomMessageFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(descriptor.getType()).thenReturn(descriptorType);

        Mockito.when(descriptorProcessor.retrieveTestAction(Mockito.any())).thenReturn(Optional.of(createTestActionWithErrors()));
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        configurationFieldModel.setFieldValue(fieldValue);
        Mockito.when(configurationFieldModelConverter.convertToConfigurationFieldModelMap(Mockito.any())).thenReturn(Map.of(ChannelDistributionUIConfig.KEY_PROVIDER_NAME, configurationFieldModel));
        Mockito.when(descriptorProcessor.retrieveTestAction(Mockito.any(), Mockito.any())).thenReturn(Optional.of(createTestActionWithIntegrationRestException()));

        ValidationActionResponse validationActionResponse = jobConfigActions.test(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void testExceptionTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));

        Descriptor descriptor = Mockito.mock(Descriptor.class);

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
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));

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
        UUID jobId = UUID.randomUUID();
        UUID newJobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModelWithValue("testValue");
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(newJobId, Set.of(createConfigurationModel()));

        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(jobAccessor.getJobByName(Mockito.anyString())).thenReturn(Optional.of(configurationJobModel));

        ValidationActionResponse validationActionResponse = jobConfigActions.validate(jobFieldModel);

        assertTrue(validationActionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, validationActionResponse.getHttpStatus());
        assertTrue(validationActionResponse.hasContent());
        ValidationResponseModel validationResponseModel = validationActionResponse.getContent().get();
        assertTrue(validationResponseModel.hasErrors());
    }

    @Test
    public void validateBadRequestWithFieldStatusTest() {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));

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
        UUID jobId = UUID.randomUUID();
        JobIdsValidationRequestModel jobIdsValidationRequestModel = new JobIdsValidationRequestModel(List.of(jobId));
        DescriptorKey descriptorKey = createDescriptorKey();
        Descriptor descriptor = createDescriptor();
        FieldModel fieldModel = createFieldModel();
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));

        Mockito.when(descriptorMap.getDescriptorMap()).thenReturn(Map.of(descriptorKey, descriptor));
        Mockito.when(authorizationManager.anyReadPermission(Mockito.any())).thenReturn(true);
        Mockito.when(jobAccessor.getJobsById(Mockito.any())).thenReturn(List.of(configurationJobModel));
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.performAfterReadAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);

        AlertFieldStatus alertFieldStatus = AlertFieldStatus.error("fieldNameTest", "Alert Error Message");
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of(alertFieldStatus));

        ActionResponse<List<JobFieldStatuses>> actionResponse = jobConfigActions.validateJobsById(jobIdsValidationRequestModel);

        assertTrue(actionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertTrue(actionResponse.hasContent());
    }

    @Test
    public void validateJobsByIdForbiddenTest() {
        UUID jobId = UUID.randomUUID();
        JobIdsValidationRequestModel jobIdsValidationRequestModel = new JobIdsValidationRequestModel(List.of(jobId));
        DescriptorKey descriptorKey = createDescriptorKey();
        Descriptor descriptor = createDescriptor();

        Mockito.when(descriptorMap.getDescriptorMap()).thenReturn(Map.of(descriptorKey, descriptor));
        Mockito.when(authorizationManager.anyReadPermission(Mockito.any())).thenReturn(false);

        ActionResponse<List<JobFieldStatuses>> actionResponse = jobConfigActions.validateJobsById(jobIdsValidationRequestModel);

        assertTrue(actionResponse.isError());
        assertEquals(HttpStatus.FORBIDDEN, actionResponse.getHttpStatus());
        assertFalse(actionResponse.hasContent());
    }

    @Test
    public void validateJobsByIdEmptyListTest() {
        JobIdsValidationRequestModel jobIdsValidationRequestModel = new JobIdsValidationRequestModel(List.of());
        DescriptorKey descriptorKey = createDescriptorKey();
        Descriptor descriptor = createDescriptor();

        Mockito.when(descriptorMap.getDescriptorMap()).thenReturn(Map.of(descriptorKey, descriptor));
        Mockito.when(authorizationManager.anyReadPermission(Mockito.any())).thenReturn(true);

        AlertFieldStatus alertFieldStatus = AlertFieldStatus.error("fieldNameTest", "Alert Error Message");
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of(alertFieldStatus));

        ActionResponse<List<JobFieldStatuses>> actionResponse = jobConfigActions.validateJobsById(jobIdsValidationRequestModel);

        assertTrue(actionResponse.isSuccessful());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
        assertTrue(actionResponse.hasContent());
    }

    @Test
    public void validateJobsByIdInternalServerErrorTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        JobIdsValidationRequestModel jobIdsValidationRequestModel = new JobIdsValidationRequestModel(List.of(jobId));
        DescriptorKey descriptorKey = createDescriptorKey();
        Descriptor descriptor = createDescriptor();
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));

        Mockito.when(descriptorMap.getDescriptorMap()).thenReturn(Map.of(descriptorKey, descriptor));
        Mockito.when(authorizationManager.anyReadPermission(Mockito.any())).thenReturn(true);
        Mockito.when(jobAccessor.getJobsById(Mockito.any())).thenReturn(List.of(configurationJobModel));

        Mockito.doThrow(new AlertDatabaseConstraintException("Exception for Alert test")).when(configurationFieldModelConverter).convertToFieldModel(Mockito.any());

        ActionResponse<List<JobFieldStatuses>> actionResponse = jobConfigActions.validateJobsById(jobIdsValidationRequestModel);

        assertTrue(actionResponse.isError());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actionResponse.getHttpStatus());
        assertFalse(actionResponse.hasContent());
    }

    @Test
    public void checkGlobalConfigExistsTest() {
        String descriptorName = "descriptorName";

        Mockito.when(globalConfigExistsValidator.validate(Mockito.any())).thenReturn(Optional.empty());

        ActionResponse<String> actionResponse = jobConfigActions.checkGlobalConfigExists(descriptorName);

        assertTrue(actionResponse.isSuccessful());
        assertEquals(HttpStatus.NO_CONTENT, actionResponse.getHttpStatus());
        assertFalse(actionResponse.hasContent());
    }

    @Test
    public void checkGlobalConfigExistsBadRequestTest() {
        String descriptorName = "descriptorName";
        String configMissingMessage = "configMissingMessageTest";

        Mockito.when(globalConfigExistsValidator.validate(Mockito.any())).thenReturn(Optional.of(configMissingMessage));
        
        ActionResponse<String> actionResponse = jobConfigActions.checkGlobalConfigExists(descriptorName);

        assertTrue(actionResponse.isError());
        assertEquals(HttpStatus.BAD_REQUEST, actionResponse.getHttpStatus());
        assertFalse(actionResponse.hasContent());
    }

    private FieldModel createFieldModel() {
        return createFieldModelWithValue("testValue");
    }

    private FieldModel createFieldModelWithValue(String value) {
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
        String fieldValue = "fieldValue";

        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        configurationFieldModel.setFieldValue(fieldValue);

        Map<String, ConfigurationFieldModel> configuredFields = Map.of(fieldKey, configurationFieldModel);
        return new ConfigurationModel(descriptorId, configurationId, createdAt, lastUpdated, configContextEnum, configuredFields);
    }

    private TestAction createTestAction() {
        TestAction testAction = new TestAction() {
            @Override
            public MessageResult testConfig(String configId, FieldModel fieldModel, FieldUtility registeredFieldValues) throws IntegrationException {
                return new MessageResult("Test Status Message");
            }
        };
        return testAction;
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
        return new DescriptorKey() {
            @Override
            public String getUniversalKey() {
                return "universal_key";
            }

            @Override
            public String getDisplayName() {
                return "Universal Key";
            }
        };
    }

    private Descriptor createDescriptor() {
        DescriptorKey descriptorKey = createDescriptorKey();
        Descriptor descriptor = new Descriptor(descriptorKey, DescriptorType.PROVIDER) {
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

    private void mockFindJobFieldModel(ConfigurationJobModel configurationJobModel, FieldModel fieldModel) throws Exception {
        Mockito.when(jobAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(configurationJobModel));
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.performAfterReadAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);
    }
}
