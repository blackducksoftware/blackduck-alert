/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Collection;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.service.email.model.EmailGlobalConfigModel;

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
        EmailGlobalConfigModel model = new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "from", "host");
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
        EmailGlobalConfigModel model = new EmailGlobalConfigModel(null, null, "from", "host");
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
            assertEquals(ConfigurationFieldValidator.REQUIRED_FIELD_MISSING_MESSAGE, status.getFieldMessage(), "Validation had unexpected field message.");
        }
    }

    @Test
    void verifyMissingAuth() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "from", "host");
        model.setSmtpAuth(true);

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(2, alertFieldStatuses.size(), "Validation found more or fewer errors than expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals(EmailGlobalConfigurationValidator.REQUIRED_BECAUSE_AUTH, status.getFieldMessage(), "Validation had unexpected field message.");
        }
    }

    @Test
    void verifyAuthNotProvided() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "from", "host");
        model.setSmtpUsername("user");
        model.setSmtpPassword("password");

        ValidationResponseModel validationResponseModel = validator.validate(model);
        assertFalse(validationResponseModel.hasErrors(), "There were errors in the configuration when none were expected.");
    }

    @Test
    void verifyMissingAuthPassword() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "from", "host");
        model.setSmtpAuth(true);
        model.setSmtpUsername("user");

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size(), "Validation found more or fewer errors than expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals("password", status.getFieldName(), "Validation reported an error for an unexpected field.");
            assertEquals(EmailGlobalConfigurationValidator.REQUIRED_BECAUSE_AUTH, status.getFieldMessage(), "Validation had unexpected field message.");
        }
    }

    @Test
    void verifyIsPasswordSet() {
        EmailGlobalConfigurationValidator validator = new EmailGlobalConfigurationValidator();
        EmailGlobalConfigModel model = new EmailGlobalConfigModel(null, AlertRestConstants.DEFAULT_CONFIGURATION_NAME, "from", "host");
        model.setSmtpAuth(true);
        model.setSmtpUsername("user");
        model.setIsSmtpPasswordSet(true);

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(0, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
    }
}
