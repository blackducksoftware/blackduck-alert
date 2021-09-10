package com.synopsys.integration.alert.component.scheduling;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptor;
import com.synopsys.integration.alert.component.scheduling.descriptor.SchedulingDescriptorKey;
import com.synopsys.integration.alert.component.scheduling.validator.SchedulingConfigurationFieldModelValidator;
import com.synopsys.integration.alert.test.common.channel.GlobalConfigurationValidatorAsserter;

public class SchedulingConfigurationValidatorTest {

    /*
     * daily processing hour: required, valid list option
     * purge data frequency: required, valid list option
     */

    @Test
    public void verifyValidConfiguration() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter();
        validatorAsserter.assertValid();
    }

    @Test
    public void missingDailyProcessing() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter();
        validatorAsserter.assertMissingValue(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY);
    }

    @Test
    public void missingPurgeFrequency() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter();
        validatorAsserter.assertMissingValue(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS);
    }

    @Test
    public void invalidOption() {
        GlobalConfigurationValidatorAsserter validatorAsserter = createValidatorAsserter();
        validatorAsserter.assertInvalidValue(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, "potato");
    }

    private GlobalConfigurationValidatorAsserter createValidatorAsserter() {
        return new GlobalConfigurationValidatorAsserter(new SchedulingDescriptorKey().getUniversalKey(), new SchedulingConfigurationFieldModelValidator(), createValidConfig());
    }

    private Map<String, FieldValueModel> createValidConfig() {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        FieldValueModel processingHour = new FieldValueModel(List.of("1"), true);
        FieldValueModel purgeFrequency = new FieldValueModel(List.of("3"), true);

        keyToValues.put(SchedulingDescriptor.KEY_DAILY_PROCESSOR_HOUR_OF_DAY, processingHour);
        keyToValues.put(SchedulingDescriptor.KEY_PURGE_DATA_FREQUENCY_DAYS, purgeFrequency);

        return keyToValues;
    }
}
