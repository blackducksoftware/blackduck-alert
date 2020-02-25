package com.synopsys.integration.alert.provider.blackduck.actions;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderLifecycleManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProperties;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.factories.BlackDuckPropertiesFactory;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckAccumulator;
import com.synopsys.integration.alert.provider.blackduck.tasks.BlackDuckDataSyncTask;
import com.synopsys.integration.function.ThrowingBiFunction;

public class BlackDuckGlobalApiActionTest {
    @Test
    public void afterSaveActionSuccessTest() throws AlertException {
        runApiActionTest(BlackDuckGlobalApiAction::afterSaveAction);
    }

    @Test
    public void afterUpdateActionSuccessTest() throws AlertException {
        runApiActionTest(BlackDuckGlobalApiAction::afterUpdateAction);
    }

    private void runApiActionTest(ThrowingBiFunction<BlackDuckGlobalApiAction, FieldModel, FieldModel, AlertException> apiAction) throws AlertException {
        TaskManager taskManager = new TaskManager();
        BlackDuckProperties properties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(properties.isConfigEnabled()).thenReturn(false);

        BlackDuckAccumulator blackDuckAccumulator = Mockito.mock(BlackDuckAccumulator.class);
        Mockito.when(blackDuckAccumulator.getTaskName()).thenReturn("accumulator-task");
        Mockito.when(blackDuckAccumulator.getFormatedNextRunTime()).thenReturn(Optional.of("SOON"));
        BlackDuckDataSyncTask blackDuckDataSyncTask = Mockito.mock(BlackDuckDataSyncTask.class);
        Mockito.when(blackDuckDataSyncTask.getTaskName()).thenReturn("data-sync-task");
        Mockito.when(blackDuckDataSyncTask.getFormatedNextRunTime()).thenReturn(Optional.of("SOON"));

        ConfigurationModel configurationModel = Mockito.mock(ConfigurationModel.class);
        Mockito.when(configurationModel.getConfigurationId()).thenReturn(-1L);
        Mockito.when(configurationModel.getDescriptorId()).thenReturn(-1L);

        ConfigurationAccessor configurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(configurationAccessor.getProviderConfigurationByName(Mockito.anyString())).thenReturn(Optional.of(configurationModel));

        BlackDuckProvider blackDuckProvider = Mockito.mock(BlackDuckProvider.class);
        Mockito.when(blackDuckProvider.validate(configurationModel)).thenReturn(true);
        Mockito.when(blackDuckProvider.createProperties(configurationModel)).thenReturn(properties);
        Mockito.when(blackDuckProvider.createProviderTasks(properties)).thenReturn(List.of(blackDuckAccumulator, blackDuckDataSyncTask));

        BlackDuckPropertiesFactory propertiesFactory = Mockito.mock(BlackDuckPropertiesFactory.class);
        Mockito.when(propertiesFactory.createProperties(Mockito.any())).thenReturn(properties);

        ProviderDataAccessor providerDataAccessor = Mockito.mock(ProviderDataAccessor.class);
        ConfigurationFieldModelConverter fieldModelConverter = Mockito.mock(ConfigurationFieldModelConverter.class);
        Mockito.when(fieldModelConverter.convertToConfigurationModel(Mockito.any())).thenReturn(configurationModel);

        ProviderLifecycleManager providerLifecycleManager = new ProviderLifecycleManager(List.of(blackDuckProvider), taskManager, null);
        BlackDuckGlobalApiAction blackDuckGlobalApiAction = new BlackDuckGlobalApiAction(blackDuckProvider, providerLifecycleManager, providerDataAccessor, fieldModelConverter, configurationAccessor);

        Optional<String> initialAccumulatorNextRunTime = taskManager.getNextRunTime(blackDuckAccumulator.getTaskName());
        Optional<String> initialSyncNextRunTime = taskManager.getNextRunTime(blackDuckDataSyncTask.getTaskName());

        assertTrue(initialAccumulatorNextRunTime.isEmpty());
        assertTrue(initialSyncNextRunTime.isEmpty());

        apiAction.apply(blackDuckGlobalApiAction, null);
        Optional<String> accumulatorNextRunTime = taskManager.getNextRunTime(blackDuckAccumulator.getTaskName());
        Optional<String> syncNextRunTime = taskManager.getNextRunTime(blackDuckDataSyncTask.getTaskName());

        assertTrue(accumulatorNextRunTime.isPresent());
        assertTrue(syncNextRunTime.isPresent());
    }

}
