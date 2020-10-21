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
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.DescriptorProcessor;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.GlobalConfigExistsValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.DescriptorAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationJobModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.FieldModelProcessor;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.common.rest.model.MultiJobFieldModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.certificates.web.PKIXErrorResponseFactory;

public class JobConfigActionsTest {
    private AuthorizationManager authorizationManager;
    private DescriptorAccessor descriptorAccessor;
    private ConfigurationAccessor configurationAccessor;
    private FieldModelProcessor fieldModelProcessor;
    private DescriptorProcessor descriptorProcessor;
    private ConfigurationFieldModelConverter configurationFieldModelConverter;
    private GlobalConfigExistsValidator globalConfigExistsValidator;
    private PKIXErrorResponseFactory pkixErrorResponseFactory;
    private DescriptorMap descriptorMap;

    @BeforeEach
    public void init() {
        authorizationManager = Mockito.mock(AuthorizationManager.class);
        descriptorAccessor = Mockito.mock(DescriptorAccessor.class);
        configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        fieldModelProcessor = Mockito.mock(FieldModelProcessor.class);
        descriptorProcessor = Mockito.mock(DescriptorProcessor.class);
        configurationFieldModelConverter = Mockito.mock(ConfigurationFieldModelConverter.class);
        globalConfigExistsValidator = Mockito.mock(GlobalConfigExistsValidator.class);
        pkixErrorResponseFactory = Mockito.mock(PKIXErrorResponseFactory.class);
        descriptorMap = Mockito.mock(DescriptorMap.class);

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
        Mockito.when(configurationAccessor.createJob(Mockito.anyCollection(), Mockito.anyCollection())).thenReturn(configurationJobModel);
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.performAfterSaveAction(Mockito.any())).thenReturn(fieldModel);

        JobConfigActions jobConfigActions = new JobConfigActions(authorizationManager, descriptorAccessor, configurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, globalConfigExistsValidator,
            pkixErrorResponseFactory, descriptorMap);

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

        JobConfigActions jobConfigActions = new JobConfigActions(authorizationManager, descriptorAccessor, configurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, globalConfigExistsValidator,
            pkixErrorResponseFactory, descriptorMap);

        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.create(jobFieldModel);

        assertTrue(jobFieldModelActionResponse.isError());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void getAllTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));

        Mockito.when(configurationAccessor.getAllJobs()).thenReturn(List.of(configurationJobModel));
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.performAfterReadAction(Mockito.any())).thenReturn(fieldModel);

        JobConfigActions jobConfigActions = new JobConfigActions(authorizationManager, descriptorAccessor, configurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, globalConfigExistsValidator,
            pkixErrorResponseFactory, descriptorMap);
        ActionResponse<MultiJobFieldModel> jobFieldModelActionResponse = jobConfigActions.getAll();

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertTrue(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void getAllServerErrorTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));

        Mockito.when(configurationAccessor.getAllJobs()).thenReturn(List.of(configurationJobModel));
        Mockito.doThrow(new AlertDatabaseConstraintException("Exception for test")).when(configurationFieldModelConverter).convertToFieldModel(Mockito.any());

        JobConfigActions jobConfigActions = new JobConfigActions(authorizationManager, descriptorAccessor, configurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, globalConfigExistsValidator,
            pkixErrorResponseFactory, descriptorMap);
        ActionResponse<MultiJobFieldModel> jobFieldModelActionResponse = jobConfigActions.getAll();

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertTrue(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void getOneTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));

        //TODO may be able to group these 3 mocks for findJobFieldModel
        Mockito.when(configurationAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(configurationJobModel));
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.performAfterReadAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);

        JobConfigActions jobConfigActions = new JobConfigActions(authorizationManager, descriptorAccessor, configurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, globalConfigExistsValidator,
            pkixErrorResponseFactory, descriptorMap);
        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.getOne(jobId);

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertTrue(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.OK, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void getOneErrorTest() throws Exception {
        UUID jobId = UUID.randomUUID();

        Mockito.doThrow(new AlertDatabaseConstraintException("Exception for Alert")).when(configurationAccessor).getJobById(Mockito.any());

        JobConfigActions jobConfigActions = new JobConfigActions(authorizationManager, descriptorAccessor, configurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, globalConfigExistsValidator,
            pkixErrorResponseFactory, descriptorMap);
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

        //TODO may be able to group these 3 mocks for findJobFieldModel
        Mockito.when(configurationAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(configurationJobModel));
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.performAfterReadAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.when(fieldModelProcessor.performBeforeUpdateAction(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.fillFieldModelWithExistingData(Mockito.anyLong(), Mockito.any())).thenReturn(List.of(configurationFieldModel));
        Mockito.when(configurationAccessor.updateJob(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(configurationJobModel);
        Mockito.when(fieldModelProcessor.performAfterUpdateAction(Mockito.any(), Mockito.any())).thenReturn(fieldModel);

        JobConfigActions jobConfigActions = new JobConfigActions(authorizationManager, descriptorAccessor, configurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, globalConfigExistsValidator,
            pkixErrorResponseFactory, descriptorMap);
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

        //TODO may be able to group these 3 mocks for findJobFieldModel
        Mockito.when(configurationAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(configurationJobModel));
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.performAfterReadAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.validateJobFieldModel(Mockito.any())).thenReturn(List.of());
        Mockito.doThrow(new AlertDatabaseConstraintException("Exception for Alert test")).when(fieldModelProcessor).performBeforeUpdateAction(Mockito.any());

        JobConfigActions jobConfigActions = new JobConfigActions(authorizationManager, descriptorAccessor, configurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, globalConfigExistsValidator,
            pkixErrorResponseFactory, descriptorMap);
        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.update(jobId, jobFieldModel);

        assertTrue(jobFieldModelActionResponse.isError());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void deleteTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        //JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));

        //TODO may be able to group these 3 mocks for findJobFieldModel
        Mockito.when(configurationAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(configurationJobModel));
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.performAfterReadAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);

        Mockito.when(fieldModelProcessor.performBeforeDeleteAction(Mockito.any())).thenReturn(fieldModel);

        JobConfigActions jobConfigActions = new JobConfigActions(authorizationManager, descriptorAccessor, configurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, globalConfigExistsValidator,
            pkixErrorResponseFactory, descriptorMap);
        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.delete(jobId);

        Mockito.verify(configurationAccessor).deleteJob(Mockito.any());
        Mockito.verify(fieldModelProcessor).performAfterDeleteAction(Mockito.any());

        assertTrue(jobFieldModelActionResponse.isSuccessful());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.NO_CONTENT, jobFieldModelActionResponse.getHttpStatus());
    }

    @Test
    public void deleteServerErrorTest() throws Exception {
        UUID jobId = UUID.randomUUID();
        FieldModel fieldModel = createFieldModel();
        //JobFieldModel jobFieldModel = new JobFieldModel(jobId.toString(), Set.of(fieldModel));
        ConfigurationJobModel configurationJobModel = new ConfigurationJobModel(jobId, Set.of(createConfigurationModel()));

        //TODO may be able to group these 3 mocks for findJobFieldModel
        Mockito.when(configurationAccessor.getJobById(Mockito.any())).thenReturn(Optional.of(configurationJobModel));
        Mockito.when(configurationFieldModelConverter.convertToFieldModel(Mockito.any())).thenReturn(fieldModel);
        Mockito.when(fieldModelProcessor.performAfterReadAction(Mockito.eq(fieldModel))).thenReturn(fieldModel);

        Mockito.doThrow(new AlertException("Exception for Alert test")).when(fieldModelProcessor).performBeforeDeleteAction(Mockito.any());

        JobConfigActions jobConfigActions = new JobConfigActions(authorizationManager, descriptorAccessor, configurationAccessor, fieldModelProcessor, descriptorProcessor, configurationFieldModelConverter, globalConfigExistsValidator,
            pkixErrorResponseFactory, descriptorMap);
        ActionResponse<JobFieldModel> jobFieldModelActionResponse = jobConfigActions.delete(jobId);

        assertTrue(jobFieldModelActionResponse.isError());
        assertFalse(jobFieldModelActionResponse.hasContent());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, jobFieldModelActionResponse.getHttpStatus());
    }

    private FieldModel createFieldModel() {
        String descriptorName = "testDescriptor";
        String emailAddress = "testEmailAddress"; //TODO may be able to make this more general. Could just be "value"

        String context = ConfigContextEnum.DISTRIBUTION.name();
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        keyToValues.put(TestAction.KEY_DESTINATION_NAME, new FieldValueModel(List.of(emailAddress), false));
        return new FieldModel(descriptorName, context, keyToValues);
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

    /*
    private ConfigurationModelMutable createConfigurationModel() {
        ConfigurationModelMutable configurationModel = new ConfigurationModelMutable(1L, 1L, "createdAt-test", "lastUpdate-test", ConfigContextEnum.DISTRIBUTION);
        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME);
        configurationFieldModel.setFieldValue("test-channel.common.name-value");
        configurationModel.put(configurationFieldModel);

        return configurationModel;
    }

     */
}
