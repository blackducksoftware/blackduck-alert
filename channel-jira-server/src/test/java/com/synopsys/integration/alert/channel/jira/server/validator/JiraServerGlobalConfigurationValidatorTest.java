package com.synopsys.integration.alert.channel.jira.server.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.util.DateUtils;

class JiraServerGlobalConfigurationValidatorTest {
    private final String ID = UUID.randomUUID().toString();
    private final String NAME = AlertRestConstants.DEFAULT_CONFIGURATION_NAME;
    private final String CREATED_AT = DateUtils.createCurrentDateTimestamp().toString();
    private final String LAST_UPDATED = DateUtils.createCurrentDateTimestamp().toString();
    private final String URL = "https://someUrl";
    private final String USER_NAME = "username";
    private final String PASSWORD = "password";

    @Test
    void verifyValidConfig() {
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(ID, NAME, CREATED_AT, LAST_UPDATED, URL, USER_NAME, PASSWORD, Boolean.FALSE, Boolean.FALSE);

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(0, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
    }

    @Test
    void verifyEmptyConfig() {
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel();

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(4, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals(ConfigurationFieldValidator.REQUIRED_FIELD_MISSING_MESSAGE, status.getFieldMessage(), "Validation had unexpected field message.");
        }
    }

    @Test
    void verifyMalformedUrl() {
        String badUrl = "notAValidUrl";
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(ID, NAME, CREATED_AT, LAST_UPDATED, badUrl, USER_NAME, PASSWORD, Boolean.FALSE, Boolean.FALSE);

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals("url", status.getFieldName(), "Validation reported an error for an unexpected field.");
        }
    }

    @Test
    void verifyPasswordIsSavedAndMissingFromModel() {
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(ID, NAME, CREATED_AT, LAST_UPDATED, URL, USER_NAME, null, Boolean.TRUE, Boolean.FALSE);

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(0, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
    }

    @Test
    void verifyPasswordIsMissingAndNotSaved() {
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator();
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(ID, NAME, URL, USER_NAME, null);

        ValidationResponseModel validationResponseModel = validator.validate(model);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals("password", status.getFieldName(), "Validation reported an error for an unexpected field.");
        }
    }

}
