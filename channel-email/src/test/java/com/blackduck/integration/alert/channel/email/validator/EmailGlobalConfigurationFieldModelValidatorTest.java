package com.blackduck.integration.alert.channel.email.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.blackduck.integration.alert.test.common.channel.GlobalConfigurationValidatorAsserter;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.descriptor.EmailChannelKey;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

class EmailGlobalConfigurationFieldModelValidatorTest {

    /*
     * Email host: Required
     * Email from: Required
     *
     * Email user: Required if email auth is true
     * Email password: Required if email auth is true
     */

    @Test
    void verifyValidConfig() {
        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = new GlobalConfigurationValidatorAsserter(
            new EmailChannelKey().getUniversalKey(),
            new EmailGlobalConfigurationFieldModelValidator(),
            createDefaultKeyToValues()
        );
        globalConfigurationValidatorAsserter.assertValid();
    }

    @Test
    void verifyMissingAuth() {
        Map<String, FieldValueModel> defaultKeyToValues = createDefaultKeyToValues();
        FieldValueModel authFieldValueModel = new FieldValueModel(List.of("true"), true);
        defaultKeyToValues.put(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), authFieldValueModel);

        FieldModel fieldModel = new FieldModel(new EmailChannelKey().getUniversalKey(), ConfigContextEnum.GLOBAL.name(), defaultKeyToValues);
        EmailGlobalConfigurationFieldModelValidator emailGlobalConfigurationValidator = new EmailGlobalConfigurationFieldModelValidator();
        Set<AlertFieldStatus> alertFieldStatuses = emailGlobalConfigurationValidator.validate(fieldModel);

        assertEquals(2, alertFieldStatuses.size());
    }

    @Test
    void verifyMissingAuthPassword() {
        Map<String, FieldValueModel> defaultKeyToValues = createDefaultKeyToValues();
        FieldValueModel authFieldValueModel = new FieldValueModel(List.of("true"), true);
        FieldValueModel usernameFieldValueModel = new FieldValueModel(List.of("username"), true);
        defaultKeyToValues.put(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), authFieldValueModel);
        defaultKeyToValues.put(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), usernameFieldValueModel);

        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = new GlobalConfigurationValidatorAsserter(
            new EmailChannelKey().getUniversalKey(),
            new EmailGlobalConfigurationFieldModelValidator(),
            defaultKeyToValues
        );
        globalConfigurationValidatorAsserter.assertMissingValue(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey());
    }

    private Map<String, FieldValueModel> createDefaultKeyToValues() {
        Map<String, FieldValueModel> keyToValues = new HashMap<>();
        FieldValueModel hostFieldValueModel = new FieldValueModel(List.of("hostName"), true);
        FieldValueModel fromFieldValueModel = new FieldValueModel(List.of("from"), true);
        keyToValues.put(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), hostFieldValueModel);
        keyToValues.put(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), fromFieldValueModel);

        return keyToValues;
    }
}
