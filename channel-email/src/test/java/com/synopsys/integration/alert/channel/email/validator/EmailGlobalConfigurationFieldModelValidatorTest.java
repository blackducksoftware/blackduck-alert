package com.synopsys.integration.alert.channel.email.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.test.common.channel.GlobalConfigurationValidatorAsserter;

public class EmailGlobalConfigurationFieldModelValidatorTest {

    /*
     * Email host: Required
     * Email from: Required
     *
     * Email user: Required if email auth is true
     * Email password: Required if email auth is true
     */

    @Test
    public void verifyValidConfig() {
        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = new GlobalConfigurationValidatorAsserter(new EmailChannelKey().getUniversalKey(), new EmailGlobalConfigurationFieldModelValidator(), createDefaultKeyToValues());
        globalConfigurationValidatorAsserter.assertValid();
    }

    @Test
    public void verifyMissingAuth() {
        Map<String, FieldValueModel> defaultKeyToValues = createDefaultKeyToValues();
        FieldValueModel authFieldValueModel = new FieldValueModel(List.of("true"), true);
        defaultKeyToValues.put(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), authFieldValueModel);

        FieldModel fieldModel = new FieldModel(new EmailChannelKey().getUniversalKey(), ConfigContextEnum.GLOBAL.name(), defaultKeyToValues);
        EmailGlobalConfigurationFieldModelValidator emailGlobalConfigurationValidator = new EmailGlobalConfigurationFieldModelValidator();
        Set<AlertFieldStatus> alertFieldStatuses = emailGlobalConfigurationValidator.validate(fieldModel);

        assertEquals(2, alertFieldStatuses.size());
    }

    @Test
    public void verifyMissingAuthPassword() {
        Map<String, FieldValueModel> defaultKeyToValues = createDefaultKeyToValues();
        FieldValueModel authFieldValueModel = new FieldValueModel(List.of("true"), true);
        FieldValueModel usernameFieldValueModel = new FieldValueModel(List.of("username"), true);
        defaultKeyToValues.put(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), authFieldValueModel);
        defaultKeyToValues.put(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), usernameFieldValueModel);

        GlobalConfigurationValidatorAsserter globalConfigurationValidatorAsserter = new GlobalConfigurationValidatorAsserter(new EmailChannelKey().getUniversalKey(), new EmailGlobalConfigurationFieldModelValidator(), defaultKeyToValues);
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
