package com.synopsys.integration.alert.channel.email.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldCommonMessageKeys;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

class EmailGlobalConfigurationValidatorTest {

    /*
     * Email host: Required
     * Email from: Required
     *
     * Email user: Required if email auth is true
     * Email password: Required if email auth is true
     */

    @Test
    void verifyValidConfig() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        model.setSmtpHost("host");
        model.setSmtpFrom("from");
        model.setSmtpAuth(true);
        model.setSmtpUsername("user");
        model.setSmtpPassword("password");

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(0, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
    }

    @Test
    void verifyNameMissingConfig() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setSmtpHost("host");
        model.setSmtpFrom("from");
        model.setSmtpAuth(true);
        model.setSmtpUsername("user");
        model.setSmtpPassword("password");

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size(), "There were errors no errors when 1 for the name was expected.");
    }

    @Test
    void verifyEmptyConfig() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(3, alertFieldStatuses.size(), "Validation found more or fewer errors than expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertTrue(StringUtils.isBlank(status.getFieldMessage()), "Validation had unexpected field message.");
            assertEquals(AlertFieldCommonMessageKeys.REQUIRED_FIELD_MISSING_KEY.name(), status.getMessageKey().orElse("UNKNOWN-KEY"), "Validation had unexpected message key");
        }
    }

    @Test
    void verifyMissingAuth() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        model.setSmtpHost("host");
        model.setSmtpFrom("from");
        model.setSmtpAuth(true);

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(2, alertFieldStatuses.size(), "Validation found more or fewer errors than expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertTrue(StringUtils.isBlank(status.getFieldMessage()), "Validation had unexpected field message.");
            assertEquals(EmailGlobalConfigurationValidator.REQUIRED_BECAUSE_AUTH, status.getMessageKey().orElse("UNKNOWN-KEY"), "Validation had unexpected message key");
        }
    }

    @Test
    void verifyAuthNotProvided() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        model.setSmtpHost("host");
        model.setSmtpFrom("from");
        model.setSmtpUsername("user");
        model.setSmtpPassword("password");

        ValidationResponseModel validationResponseModel = validator.validate(model);
        assertFalse(validationResponseModel.hasErrors(), "There were errors in the configuration when none were expected.");
    }

    @Test
    void verifyMissingAuthPassword() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        model.setSmtpHost("host");
        model.setSmtpFrom("from");
        model.setSmtpAuth(true);
        model.setSmtpUsername("user");

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size(), "Validation found more or fewer errors than expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {

            assertTrue(StringUtils.isBlank(status.getFieldMessage()), "Validation had unexpected field message.");
            assertEquals(EmailGlobalConfigurationValidator.REQUIRED_BECAUSE_AUTH, status.getMessageKey().orElse("UNKNOWN-KEY"), "Validation had unexpected message key");
        }
    }

    @Test
    void verifyIsPasswordSet() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME);
        model.setSmtpHost("host");
        model.setSmtpFrom("from");
        model.setSmtpAuth(true);
        model.setSmtpUsername("user");
        model.setIsSmtpPasswordSet(true);

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(0, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
    }
}
