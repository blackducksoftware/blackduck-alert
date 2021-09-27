package com.synopsys.integration.alert.channel.email.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

public class EmailGlobalConfigurationValidatorTest {

    /*
     * Email host: Required
     * Email from: Required
     *
     * Email user: Required if email auth is true
     * Email password: Required if email auth is true
     */

    @Test
    public void verifyValidConfig() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setHost("host");
        model.setFrom("from");
        model.setAuth(true);
        model.setUsername("user");
        model.setPassword("password");

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(0, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
    }

    @Test
    public void verifyEmptyConfig() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(2, alertFieldStatuses.size(), "Validation found more or fewer errors than expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals(ConfigurationFieldValidator.REQUIRED_FIELD_MISSING_MESSAGE, status.getFieldMessage(), "Validation had unexpected field message.");
        }
    }

    @Test
    public void verifyMissingAuth() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setHost("host");
        model.setFrom("from");
        model.setAuth(true);

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(2, alertFieldStatuses.size(), "Validation found more or fewer errors than expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals(EmailGlobalConfigurationValidator.REQUIRED_BECAUSE_AUTH, status.getFieldMessage(), "Validation had unexpected field message.");
        }
    }

    @Test
    public void verifyAuthNotProvided() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setHost("host");
        model.setFrom("from");
        model.setUsername("user");
        model.setPassword("password");

        ValidationResponseModel validationResponseModel = validator.validate(model);
        assertFalse(validationResponseModel.hasErrors(), "There were errors in the configuration when none were expected.");
    }

    @Test
    public void verifyMissingAuthPassword() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.setHost("host");
        model.setFrom("from");
        model.setAuth(true);
        model.setUsername("user");

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size(), "Validation found more or fewer errors than expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals("password", status.getFieldName(), "Validation reported an error for an unexpected field.");
            assertEquals(EmailGlobalConfigurationValidator.REQUIRED_BECAUSE_AUTH, status.getFieldMessage(), "Validation had unexpected field message.");
        }
    }
}
