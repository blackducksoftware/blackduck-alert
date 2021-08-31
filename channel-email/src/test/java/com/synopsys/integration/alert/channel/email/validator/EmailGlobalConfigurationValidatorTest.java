package com.synopsys.integration.alert.channel.email.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.channel.email.web.EmailGlobalConfigModel;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;

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
        model.host = "host";
        model.from = "from";
        model.auth = true;
        model.user = "user";
        model.password = "password";

        Set<AlertFieldStatus> alertFieldStatuses = validator.validate(model);
        assertEquals(0, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
    }

    @Test
    public void verifyEmptyConfig() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();

        Set<AlertFieldStatus> alertFieldStatuses = validator.validate(model);
        assertEquals(2, alertFieldStatuses.size(), "Validation found more or fewer errors than expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals(ConfigurationFieldValidator.REQUIRED_FIELD_MISSING_MESSAGE, status.getFieldMessage(), "Validation had unexpected field message.");
        }
    }

    @Test
    public void verifyMissingAuth() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.host = "host";
        model.from = "from";
        model.auth = true;

        Set<AlertFieldStatus> alertFieldStatuses = validator.validate(model);
        assertEquals(2, alertFieldStatuses.size(), "Validation found more or fewer errors than expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals(EmailGlobalConfigurationValidator.REQUIRED_BECAUSE_AUTH, status.getFieldMessage(), "Validation had unexpected field message.");
        }
    }

    @Test
    public void verifyAuthNotProvided() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.host = "host";
        model.from = "from";
        model.user = "user";
        model.password = "user";

        Set<AlertFieldStatus> alertFieldStatuses = validator.validate(model);
        assertEquals(0, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
    }

    @Test
    public void verifyMissingAuthPassword() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel();
        model.host = "host";
        model.from = "from";
        model.auth = true;
        model.user = "user";

        Set<AlertFieldStatus> alertFieldStatuses = validator.validate(model);
        assertEquals(1, alertFieldStatuses.size(), "Validation found more or fewer errors than expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals("password", status.getFieldName(), "Validation reported an error for an unexpected field.");
            assertEquals(EmailGlobalConfigurationValidator.REQUIRED_BECAUSE_AUTH, status.getFieldMessage(), "Validation had unexpected field message.");
        }
    }
}
