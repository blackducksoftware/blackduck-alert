package com.synopsys.integration.alert.component.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.context.DescriptorActionApi;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.workflow.TaskManager;
import com.synopsys.integration.alert.web.model.configuration.FieldModel;
import com.synopsys.integration.alert.web.model.configuration.FieldValueModel;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;
import com.synopsys.integration.exception.IntegrationException;

public class SchedulingDescriptorActionApiTest {
    public static final String DAILY_DIGEST_ERROR_MESSAGE = "Must be a number between 0 and 23";
    public static final String PURGE_FREQUENCY_ERROR_MESSAGE = "Must be a number between 1 and 7";
    private static final FieldValueModel FIELD_HOUR_OF_DAY = new FieldValueModel(new ArrayList<>(), false);
    private static final FieldValueModel FIELD_PURGE_FREQUENCY = new FieldValueModel(new ArrayList<>(), false);
    private static final Map<String, FieldValueModel> FIELD_MAP = Map.of(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, FIELD_HOUR_OF_DAY,
        SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, FIELD_PURGE_FREQUENCY);
    private static final FieldModel FIELD_MODEL = new FieldModel(SchedulingDescriptor.SCHEDULING_COMPONENT, ConfigContextEnum.GLOBAL.name(), FIELD_MAP);

    @AfterEach
    public void cleanup() {
        FIELD_HOUR_OF_DAY.setValue("");
        FIELD_PURGE_FREQUENCY.setValue("");
    }

    @Test
    public void validateConfigWithNoErrorsTest() {
        final Map<String, String> fieldErrors = new HashMap<>();
        final SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final SchedulingDescriptorActionApi schedulingActionApi = new SchedulingDescriptorActionApi(taskManager);
        final SchedulingDescriptor schedulingDescriptor = new SchedulingDescriptor(schedulingActionApi, schedulingUIConfig);
        final Optional<DescriptorActionApi> actionApiOptional = schedulingDescriptor.getActionApi(ConfigContextEnum.GLOBAL);
        assertTrue(actionApiOptional.isPresent());
        final DescriptorActionApi actionApi = actionApiOptional.get();

        FIELD_HOUR_OF_DAY.setValue("1");
        FIELD_PURGE_FREQUENCY.setValue("1");
        final Map<String, ConfigField> configFieldMap = schedulingUIConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        actionApi.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);
        assertEquals(null, fieldErrors.get(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY));
        assertEquals(null, fieldErrors.get(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS));
    }

    @Test
    public void validateConfigHasErrorWhenEmptyStringTest() {
        final Map<String, String> fieldErrors = new HashMap<>();
        final SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final SchedulingDescriptorActionApi schedulingActionApi = new SchedulingDescriptorActionApi(taskManager);
        final SchedulingDescriptor schedulingDescriptor = new SchedulingDescriptor(schedulingActionApi, schedulingUIConfig);
        final Optional<DescriptorActionApi> actionApiOptional = schedulingDescriptor.getActionApi(ConfigContextEnum.GLOBAL);
        assertTrue(actionApiOptional.isPresent());
        final DescriptorActionApi actionApi = actionApiOptional.get();

        FIELD_HOUR_OF_DAY.setValue("");
        FIELD_PURGE_FREQUENCY.setValue("");
        final Map<String, ConfigField> configFieldMap = schedulingUIConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        actionApi.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY));
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS));
    }

    @Test
    public void validateConfigHasErrorWhenValuesNotNumericTest() {
        final Map<String, String> fieldErrors = new HashMap<>();
        final SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final SchedulingDescriptorActionApi schedulingActionApi = new SchedulingDescriptorActionApi(taskManager);
        final SchedulingDescriptor schedulingDescriptor = new SchedulingDescriptor(schedulingActionApi, schedulingUIConfig);
        final Optional<DescriptorActionApi> actionApiOptional = schedulingDescriptor.getActionApi(ConfigContextEnum.GLOBAL);
        assertTrue(actionApiOptional.isPresent());
        final DescriptorActionApi actionApi = actionApiOptional.get();

        FIELD_HOUR_OF_DAY.setValue("not a number");
        FIELD_PURGE_FREQUENCY.setValue("not a number");
        final Map<String, ConfigField> configFieldMap = schedulingUIConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        actionApi.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);
        assertEquals(DAILY_DIGEST_ERROR_MESSAGE, fieldErrors.get(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY));
        assertEquals(PURGE_FREQUENCY_ERROR_MESSAGE, fieldErrors.get(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS));
    }

    @Test
    public void validateConfigHasErrorWhenHourOutOfRangeTest() {
        final Map<String, String> fieldErrors = new HashMap<>();
        final SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final SchedulingDescriptorActionApi schedulingActionApi = new SchedulingDescriptorActionApi(taskManager);
        final SchedulingDescriptor schedulingDescriptor = new SchedulingDescriptor(schedulingActionApi, schedulingUIConfig);
        final Optional<DescriptorActionApi> actionApiOptional = schedulingDescriptor.getActionApi(ConfigContextEnum.GLOBAL);
        assertTrue(actionApiOptional.isPresent());
        final DescriptorActionApi actionApi = actionApiOptional.get();

        FIELD_HOUR_OF_DAY.setValue("-1");
        final Map<String, ConfigField> configFieldMap = schedulingUIConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        actionApi.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);
        assertEquals(DAILY_DIGEST_ERROR_MESSAGE, fieldErrors.get(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY));

        fieldErrors.clear();
        FIELD_HOUR_OF_DAY.setValue("24");
        actionApi.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);
        assertEquals(DAILY_DIGEST_ERROR_MESSAGE, fieldErrors.get(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY));
    }

    @Test
    public void validateConfigHasErrorWhenPurgeFrequencyOutOfRangeTest() {
        final Map<String, String> fieldErrors = new HashMap<>();
        final SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final SchedulingDescriptorActionApi schedulingActionApi = new SchedulingDescriptorActionApi(taskManager);
        final SchedulingDescriptor schedulingDescriptor = new SchedulingDescriptor(schedulingActionApi, schedulingUIConfig);
        final Optional<DescriptorActionApi> actionApiOptional = schedulingDescriptor.getActionApi(ConfigContextEnum.GLOBAL);
        assertTrue(actionApiOptional.isPresent());
        final DescriptorActionApi actionApi = actionApiOptional.get();

        FIELD_PURGE_FREQUENCY.setValue("0");
        final Map<String, ConfigField> configFieldMap = schedulingUIConfig.createFields().stream()
                                                            .collect(Collectors.toMap(ConfigField::getKey, Function.identity()));
        actionApi.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);
        assertEquals(PURGE_FREQUENCY_ERROR_MESSAGE, fieldErrors.get(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS));

        fieldErrors.clear();
        FIELD_PURGE_FREQUENCY.setValue("8");
        actionApi.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);

        assertEquals(PURGE_FREQUENCY_ERROR_MESSAGE, fieldErrors.get(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS));
    }

    @Test
    public void testConfigTest() {
        final SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final SchedulingDescriptorActionApi schedulingActionApi = new SchedulingDescriptorActionApi(taskManager);
        final SchedulingDescriptor schedulingDescriptor = new SchedulingDescriptor(schedulingActionApi, schedulingUIConfig);
        final Optional<DescriptorActionApi> actionApiOptional = schedulingDescriptor.getActionApi(ConfigContextEnum.GLOBAL);
        assertTrue(actionApiOptional.isPresent());
        final DescriptorActionApi actionApi = actionApiOptional.get();

        try {
            actionApi.testConfig(null);
            fail("Expected exception to be thrown");
        } catch (final IntegrationException e) {
            assertEquals("Method not allowed. - Component descriptors cannot be tested.", e.getMessage());
        }
    }

    @Test
    public void testReadConfig() {
        final Long accumulatorTime = 998L;
        final String nextRunTimeString = "task_next_run_time";
        final SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        Mockito.when(taskManager.getDifferenceToNextRun(Mockito.anyString(), Mockito.any(TimeUnit.class))).thenReturn(Optional.of(accumulatorTime));
        Mockito.when(taskManager.getNextRunTime(Mockito.anyString())).thenReturn(Optional.of(nextRunTimeString));

        final SchedulingDescriptorActionApi schedulingActionApi = new SchedulingDescriptorActionApi(taskManager);
        final SchedulingDescriptor schedulingDescriptor = new SchedulingDescriptor(schedulingActionApi, schedulingUIConfig);
        final Optional<DescriptorActionApi> actionApiOptional = schedulingDescriptor.getActionApi(ConfigContextEnum.GLOBAL);
        assertTrue(actionApiOptional.isPresent());
        final DescriptorActionApi actionApi = actionApiOptional.get();
        final FieldModel fieldModel = new FieldModel(SchedulingDescriptor.SCHEDULING_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        final FieldModel actualFieldModel = actionApi.readConfig(fieldModel);
        final Optional<String> accumulatorNextRun = actualFieldModel.getFieldValue(SchedulingDescriptor.KEY_ACCUMULATOR_NEXT_RUN);
        final Optional<String> dailyTaskNextRun = actualFieldModel.getFieldValue(SchedulingDescriptor.KEY_DAILY_PROCESSOR_NEXT_RUN);
        final Optional<String> purgeTaskNextRun = actualFieldModel.getFieldValue(SchedulingDescriptor.KEY_PURGE_DATA_NEXT_RUN);

        assertTrue(accumulatorNextRun.isPresent());
        assertTrue(dailyTaskNextRun.isPresent());
        assertTrue(purgeTaskNextRun.isPresent());

        assertEquals(accumulatorTime, Long.valueOf(accumulatorNextRun.get()));
        assertEquals(nextRunTimeString, dailyTaskNextRun.get());
        assertEquals(nextRunTimeString, purgeTaskNextRun.get());
    }

    @Test
    public void testUpdateConfig() {
        final SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final SchedulingDescriptorActionApi schedulingActionApi = new SchedulingDescriptorActionApi(taskManager);
        final SchedulingDescriptor schedulingDescriptor = new SchedulingDescriptor(schedulingActionApi, schedulingUIConfig);
        final Optional<DescriptorActionApi> actionApiOptional = schedulingDescriptor.getActionApi(ConfigContextEnum.GLOBAL);
        assertTrue(actionApiOptional.isPresent());
        final DescriptorActionApi actionApi = actionApiOptional.get();

        final FieldModel fieldModel = new FieldModel(SchedulingDescriptor.SCHEDULING_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, new FieldValueModel(List.of("1"), false));
        fieldModel.putField(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, new FieldValueModel(List.of("5"), false));
        actionApi.updateConfig(fieldModel);
        Mockito.verify(taskManager).scheduleCronTask(Mockito.anyString(), Mockito.eq(DailyTask.TASK_NAME));
        Mockito.verify(taskManager).scheduleCronTask(Mockito.anyString(), Mockito.eq(PurgeTask.TASK_NAME));
    }

    @Test
    public void testSaveConfig() {
        final SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        final TaskManager taskManager = Mockito.mock(TaskManager.class);
        final SchedulingDescriptorActionApi schedulingActionApi = new SchedulingDescriptorActionApi(taskManager);
        final SchedulingDescriptor schedulingDescriptor = new SchedulingDescriptor(schedulingActionApi, schedulingUIConfig);
        final Optional<DescriptorActionApi> actionApiOptional = schedulingDescriptor.getActionApi(ConfigContextEnum.GLOBAL);
        assertTrue(actionApiOptional.isPresent());
        final DescriptorActionApi actionApi = actionApiOptional.get();

        final FieldModel fieldModel = new FieldModel(SchedulingDescriptor.SCHEDULING_COMPONENT, ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, new FieldValueModel(List.of("2"), false));
        fieldModel.putField(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, new FieldValueModel(List.of("6"), false));
        actionApi.saveConfig(fieldModel);
        Mockito.verify(taskManager).scheduleCronTask(Mockito.anyString(), Mockito.eq(DailyTask.TASK_NAME));
        Mockito.verify(taskManager).scheduleCronTask(Mockito.anyString(), Mockito.eq(PurgeTask.TASK_NAME));
    }
}
