package com.synopsys.integration.alert.provider.blackduck.action;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.api.provider.lifecycle.ProviderSchedulingManager;
import com.synopsys.integration.alert.api.provider.state.StatefulProvider;
import com.synopsys.integration.alert.api.task.TaskManager;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.descriptor.api.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckCacheHttpClientCache;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.factory.BlackDuckPropertiesFactory;
import com.synopsys.integration.alert.provider.blackduck.task.BlackDuckDataSyncTask;
import com.synopsys.integration.alert.provider.blackduck.task.accumulator.BlackDuckAccumulator;
import com.synopsys.integration.function.ThrowingBiFunction;

public class BlackDuckGlobalApiActionTest {
    @Test
    public void afterSaveActionSuccessTest() throws AlertException {
        runApiActionTest(BlackDuckGlobalApiAction::afterSaveAction);
    }

    @Test
    public void afterUpdateActionSuccessTest() throws AlertException {
        runApiActionTest((blackDuckGlobalApiAction, fieldModel) -> blackDuckGlobalApiAction.afterUpdateAction(fieldModel, fieldModel));
    }

    private void runApiActionTest(ThrowingBiFunction<BlackDuckGlobalApiAction, FieldModel, FieldModel, AlertException> apiAction) throws AlertException {
        TaskManager taskManager = new TaskManager();
        BlackDuckProperties properties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(properties.isConfigEnabled()).thenReturn(true);
        FieldModel fieldModel = Mockito.mock(FieldModel.class);
        Mockito.when(fieldModel.getId()).thenReturn("1");
        Mockito.when(fieldModel.getFieldValue(Mockito.eq(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED))).thenReturn(Optional.of("true"));
        String providerConfigName = "Test Provider Config";
        Mockito.when(fieldModel.getFieldValue(Mockito.eq(ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME))).thenReturn(Optional.of(providerConfigName));

        BlackDuckAccumulator blackDuckAccumulator = Mockito.mock(BlackDuckAccumulator.class);
        Mockito.when(blackDuckAccumulator.getTaskName()).thenReturn("accumulator-task");
        Mockito.when(blackDuckAccumulator.getFormatedNextRunTime()).thenReturn(Optional.of("SOON"));
        BlackDuckDataSyncTask blackDuckDataSyncTask = Mockito.mock(BlackDuckDataSyncTask.class);
        Mockito.when(blackDuckDataSyncTask.getTaskName()).thenReturn("data-sync-task");
        Mockito.when(blackDuckDataSyncTask.getFormatedNextRunTime()).thenReturn(Optional.of("SOON"));

        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        Long providerConfigId = -1L;
        Mockito.when(configurationModel.getConfigurationId()).thenReturn(providerConfigId);
        Mockito.when(configurationModel.getDescriptorId()).thenReturn(providerConfigId);

        ConfigurationFieldModel configurationFieldModel = ConfigurationFieldModel.create(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED);
        configurationFieldModel.setFieldValue("true");
        Mockito.when(configurationModel.getField(ProviderDescriptor.KEY_PROVIDER_CONFIG_ENABLED)).thenReturn(Optional.of(configurationFieldModel));

        ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor = Mockito.mock(ConfigurationModelConfigurationAccessor.class);
        Mockito.when(configurationModelConfigurationAccessor.getProviderConfigurationByName(Mockito.anyString())).thenReturn(Optional.of(configurationModel));
        Mockito.when(configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(Mockito.any(BlackDuckProviderKey.class), Mockito.eq(ConfigContextEnum.DISTRIBUTION))).thenReturn(List.of());

        BlackDuckProviderKey blackDuckProviderKey = new BlackDuckProviderKey();
        StatefulProvider statefulProvider = Mockito.mock(StatefulProvider.class);
        BlackDuckProvider blackDuckProvider = Mockito.mock(BlackDuckProvider.class);
        Mockito.when(blackDuckProvider.validate(configurationModel)).thenReturn(true);
        Mockito.when(blackDuckProvider.createStatefulProvider(configurationModel)).thenReturn(statefulProvider);
        Mockito.when(statefulProvider.getKey()).thenReturn(blackDuckProviderKey);
        Mockito.when(statefulProvider.getConfigId()).thenReturn(providerConfigId);
        Mockito.when(statefulProvider.isConfigEnabled()).thenReturn(true);
        Mockito.when(statefulProvider.getConfigName()).thenReturn(providerConfigName);
        Mockito.when(statefulProvider.getProperties()).thenReturn(properties);
        Mockito.when(statefulProvider.getTasks()).thenReturn(List.of(blackDuckAccumulator, blackDuckDataSyncTask));

        BlackDuckPropertiesFactory propertiesFactory = Mockito.mock(BlackDuckPropertiesFactory.class);
        Mockito.when(propertiesFactory.createProperties((ConfigurationModel) Mockito.any())).thenReturn(properties);

        ProviderDataAccessor providerDataAccessor = Mockito.mock(ProviderDataAccessor.class);
        ConfigurationFieldModelConverter fieldModelConverter = Mockito.mock(ConfigurationFieldModelConverter.class);
        Mockito.when(fieldModelConverter.convertToConfigurationModel(Mockito.any())).thenReturn(configurationModel);

        BlackDuckCacheHttpClientCache blackDuckCacheHttpClientCache = Mockito.mock(BlackDuckCacheHttpClientCache.class);

        ProviderSchedulingManager providerLifecycleManager = new ProviderSchedulingManager(List.of(blackDuckProvider), taskManager, null);
        BlackDuckGlobalApiAction blackDuckGlobalApiAction = new BlackDuckGlobalApiAction(blackDuckProvider, providerLifecycleManager, providerDataAccessor, configurationModelConfigurationAccessor, blackDuckCacheHttpClientCache);

        Optional<String> initialAccumulatorNextRunTime = taskManager.getNextRunTime(blackDuckAccumulator.getTaskName());
        Optional<String> initialSyncNextRunTime = taskManager.getNextRunTime(blackDuckDataSyncTask.getTaskName());

        assertTrue(initialAccumulatorNextRunTime.isEmpty());
        assertTrue(initialSyncNextRunTime.isEmpty());

        apiAction.apply(blackDuckGlobalApiAction, fieldModel);
        Optional<String> accumulatorNextRunTime = taskManager.getNextRunTime(blackDuckAccumulator.getTaskName());
        Optional<String> syncNextRunTime = taskManager.getNextRunTime(blackDuckDataSyncTask.getTaskName());

        assertTrue(accumulatorNextRunTime.isPresent(), "The accumulator task next run time was not present");
        assertTrue(syncNextRunTime.isPresent(), "The sync task next run time was not present");
    }

}
