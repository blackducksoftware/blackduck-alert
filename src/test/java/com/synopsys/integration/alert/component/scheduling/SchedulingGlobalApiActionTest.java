package com.synopsys.integration.alert.component.scheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.SelectConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.common.util.DataStructureUtils;
import com.synopsys.integration.alert.common.workflow.task.ScheduledTask;
import com.synopsys.integration.alert.common.workflow.task.TaskManager;
import com.synopsys.integration.alert.component.scheduling.actions.SchedulingGlobalApiAction;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptorKey;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingUIConfig;
import com.synopsys.integration.alert.web.config.FieldValidationAction;
import com.synopsys.integration.alert.workflow.scheduled.PurgeTask;
import com.synopsys.integration.alert.workflow.scheduled.frequency.DailyTask;

public class SchedulingGlobalApiActionTest {
    private static final SchedulingDescriptorKey SCHEDULING_DESCRIPTOR_KEY = new SchedulingDescriptorKey();
    private static final FieldValueModel FIELD_HOUR_OF_DAY = new FieldValueModel(new ArrayList<>(), false);
    private static final FieldValueModel FIELD_PURGE_FREQUENCY = new FieldValueModel(new ArrayList<>(), false);
    private static final Map<String, FieldValueModel> FIELD_MAP = Map.of(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, FIELD_HOUR_OF_DAY,
        SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, FIELD_PURGE_FREQUENCY);
    private static final FieldModel FIELD_MODEL = new FieldModel(SCHEDULING_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), FIELD_MAP);

    @AfterEach
    public void cleanup() {
        FIELD_HOUR_OF_DAY.setValue("");
        FIELD_PURGE_FREQUENCY.setValue("");
    }

    @Test
    public void validateConfigWithNoErrorsTest() {
        Map<String, AlertFieldStatus> fieldErrors = new HashMap<>();
        SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        schedulingUIConfig.setConfigFields();

        FIELD_HOUR_OF_DAY.setValue("1");
        FIELD_PURGE_FREQUENCY.setValue("1");
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(schedulingUIConfig.getFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);
        assertEquals(null, fieldErrors.get(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY));
        assertEquals(null, fieldErrors.get(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS));
    }

    @Test
    public void validateConfigHasErrorWhenEmptyStringTest() {
        Map<String, AlertFieldStatus> fieldErrors = new HashMap<>();
        SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        schedulingUIConfig.setConfigFields();

        FIELD_HOUR_OF_DAY.setValue("");
        FIELD_PURGE_FREQUENCY.setValue("");
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(schedulingUIConfig.getFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY).getFieldMessage());
        assertEquals(ConfigField.REQUIRED_FIELD_MISSING, fieldErrors.get(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS).getFieldMessage());
    }

    @Test
    public void validateConfigHasErrorWhenValuesNotNumericTest() {
        Map<String, AlertFieldStatus> fieldErrors = new HashMap<>();
        SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        schedulingUIConfig.setConfigFields();

        FIELD_HOUR_OF_DAY.setValue("not a number");
        FIELD_PURGE_FREQUENCY.setValue("not a number");
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(schedulingUIConfig.getFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);

        String actualDailyProcessorError = fieldErrors.get(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY).getFieldMessage();
        assertTrue(actualDailyProcessorError.contains(SelectConfigField.INVALID_OPTION_SELECTED), "Expected to contain: " + SelectConfigField.INVALID_OPTION_SELECTED + ". Actual: " + actualDailyProcessorError);

        String actualPurgeDataError = fieldErrors.get(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS).getFieldMessage();
        assertTrue(actualPurgeDataError.contains(SelectConfigField.INVALID_OPTION_SELECTED), "Expected to contain: " + SelectConfigField.INVALID_OPTION_SELECTED + ". Actual: " + actualPurgeDataError);

    }

    @Test
    public void validateConfigHasErrorWhenHourOutOfRangeTest() {
        Map<String, AlertFieldStatus> fieldErrors = new HashMap<>();
        SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        schedulingUIConfig.setConfigFields();

        FIELD_HOUR_OF_DAY.setValue("-1");
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(schedulingUIConfig.getFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);

        String actualError = fieldErrors.get(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY).getFieldMessage();
        assertTrue(actualError.contains(SelectConfigField.INVALID_OPTION_SELECTED), "Expected to contain: " + SelectConfigField.INVALID_OPTION_SELECTED + ". Actual: " + actualError);

        fieldErrors.clear();
        FIELD_HOUR_OF_DAY.setValue("24");
        fieldValidationAction.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);

        actualError = fieldErrors.get(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY).getFieldMessage();
        assertTrue(actualError.contains(SelectConfigField.INVALID_OPTION_SELECTED), "Expected to contain: " + SelectConfigField.INVALID_OPTION_SELECTED + ". Actual: " + actualError);
    }

    @Test
    public void validateConfigHasErrorWhenPurgeFrequencyOutOfRangeTest() {
        Map<String, AlertFieldStatus> fieldErrors = new HashMap<>();
        SchedulingUIConfig schedulingUIConfig = new SchedulingUIConfig();
        schedulingUIConfig.setConfigFields();

        FIELD_PURGE_FREQUENCY.setValue("0");
        Map<String, ConfigField> configFieldMap = DataStructureUtils.mapToValues(schedulingUIConfig.getFields(), ConfigField::getKey);
        FieldValidationAction fieldValidationAction = new FieldValidationAction();
        fieldValidationAction.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);
        String actualError = fieldErrors.get(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS).getFieldMessage();
        assertTrue(actualError.contains(SelectConfigField.INVALID_OPTION_SELECTED), "Expected to contain: " + SelectConfigField.INVALID_OPTION_SELECTED + ". Actual: " + actualError);

        fieldErrors.clear();
        FIELD_PURGE_FREQUENCY.setValue("8");
        fieldValidationAction.validateConfig(configFieldMap, FIELD_MODEL, fieldErrors);

        actualError = fieldErrors.get(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS).getFieldMessage();
        assertTrue(actualError.contains(SelectConfigField.INVALID_OPTION_SELECTED), "Expected to contain: " + SelectConfigField.INVALID_OPTION_SELECTED + ". Actual: " + actualError);
    }

    @Test
    public void testReadConfig() {
        final Long accumulatorTime = 998L;
        final String nextRunTimeString = "task_next_run_time";
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        Mockito.when(taskManager.getDifferenceToNextRun(Mockito.anyString(), Mockito.any(TimeUnit.class))).thenReturn(Optional.of(accumulatorTime));
        Mockito.when(taskManager.getNextRunTime(Mockito.anyString())).thenReturn(Optional.of(nextRunTimeString));

        SchedulingGlobalApiAction schedulingActionApi = new SchedulingGlobalApiAction(taskManager);
        FieldModel fieldModel = new FieldModel(SCHEDULING_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        FieldModel actualFieldModel = schedulingActionApi.afterGetAction(fieldModel);
        Optional<String> dailyTaskNextRun = actualFieldModel.getFieldValue(SchedulingDescriptor.KEY_DAILY_PROCESSOR_NEXT_RUN);
        Optional<String> purgeTaskNextRun = actualFieldModel.getFieldValue(SchedulingDescriptor.KEY_PURGE_DATA_NEXT_RUN);

        assertTrue(dailyTaskNextRun.isPresent());
        assertTrue(purgeTaskNextRun.isPresent());

        assertEquals(nextRunTimeString, dailyTaskNextRun.get());
        assertEquals(nextRunTimeString, purgeTaskNextRun.get());
    }

    @Test
    public void testUpdateConfig() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        SchedulingGlobalApiAction schedulingActionApi = new SchedulingGlobalApiAction(taskManager);

        FieldModel fieldModel = new FieldModel(SCHEDULING_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, new FieldValueModel(List.of("1"), false));
        fieldModel.putField(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, new FieldValueModel(List.of("5"), false));
        schedulingActionApi.handleNewAndSavedConfig(fieldModel);
        Mockito.verify(taskManager).scheduleCronTask(Mockito.anyString(), Mockito.eq(ScheduledTask.computeTaskName(DailyTask.class)));
        Mockito.verify(taskManager).scheduleCronTask(Mockito.anyString(), Mockito.eq(ScheduledTask.computeTaskName(PurgeTask.class)));
    }

    @Test
    public void testSaveConfig() {
        TaskManager taskManager = Mockito.mock(TaskManager.class);
        SchedulingGlobalApiAction actionApi = new SchedulingGlobalApiAction(taskManager);

        FieldModel fieldModel = new FieldModel(SCHEDULING_DESCRIPTOR_KEY.getUniversalKey(), ConfigContextEnum.GLOBAL.name(), new HashMap<>());
        fieldModel.putField(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, new FieldValueModel(List.of("2"), false));
        fieldModel.putField(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, new FieldValueModel(List.of("6"), false));
        actionApi.handleNewAndSavedConfig(fieldModel);
        Mockito.verify(taskManager).scheduleCronTask(Mockito.anyString(), Mockito.eq(ScheduledTask.computeTaskName(DailyTask.class)));
        Mockito.verify(taskManager).scheduleCronTask(Mockito.anyString(), Mockito.eq(ScheduledTask.computeTaskName(PurgeTask.class)));
    }

}
