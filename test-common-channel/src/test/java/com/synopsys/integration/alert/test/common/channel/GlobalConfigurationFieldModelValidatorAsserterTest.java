package com.synopsys.integration.alert.test.common.channel;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;

public class GlobalConfigurationFieldModelValidatorAsserterTest {
    private static final String CLASS_NAME = GlobalConfigurationFieldModelValidatorAsserterTest.class.getSimpleName();

    @Test
    public void assertInvalidValueTest() {
        String key = "valid.key";
        GlobalConfigurationValidatorAsserter asserter = createAsserter(key);
        asserter.assertInvalidValue(key, "invalid");
    }

    @Test
    public void assertMissingValueTest() {
        String key = "a.key";
        GlobalConfigurationValidatorAsserter asserter = createAsserter(key);
        asserter.assertMissingValue(key);
    }

    @Test
    public void assertCustomTest() {
        GlobalConfigurationValidatorAsserter asserter = createAsserter(Set.of());
        asserter.assertCustom(fieldStatuses -> assertTrue(fieldStatuses.isEmpty(), "Expected field statuses to be empty"));
    }

    @Test
    public void assertValidTest() {
        GlobalConfigurationValidatorAsserter asserter = createAsserter(Set.of());
        asserter.assertValid();
    }

    private static GlobalConfigurationValidatorAsserter createAsserter(String key) {
        AlertFieldStatus fieldStatus = AlertFieldStatus.error(key, "error");
        return createAsserter(Set.of(fieldStatus));
    }

    private static GlobalConfigurationValidatorAsserter createAsserter(Set<AlertFieldStatus> fieldStatuses) {
        GlobalConfigurationFieldModelValidator globalConfigurationFieldModelValidator = fieldModel -> fieldStatuses;
        return new GlobalConfigurationValidatorAsserter(CLASS_NAME, globalConfigurationFieldModelValidator, Map.of());
    }

}
