package com.synopsys.integration.alert.channel.jira.server.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.channel.jira.server.database.accessor.JiraServerGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.jira.server.model.JiraServerGlobalConfigModel;
import com.synopsys.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.util.DateUtils;

class JiraServerGlobalConfigurationValidatorTest {
    private final String ID = UUID.randomUUID().toString();
    private final String NAME = AlertRestConstants.DEFAULT_CONFIGURATION_NAME;
    private final String CREATED_AT = DateUtils.createCurrentDateTimestamp().toString();
    private final String LAST_UPDATED = DateUtils.createCurrentDateTimestamp().toString();
    private final String URL = "https://someUrl";
    private final String USER_NAME = "username";
    private final String PASSWORD = "password";

    // TODO: Implement access token and AuthorizationMethod
    //  New set of validation should be implemented to support both Auth methods
    @Test
    void verifyValidConfig() {
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            ID,
            NAME,
            CREATED_AT,
            LAST_UPDATED,
            URL,
            JiraServerAuthorizationMethod.BASIC,
            USER_NAME,
            PASSWORD,
            Boolean.FALSE,
            null,
            Boolean.FALSE,
            Boolean.FALSE
        );

        ValidationResponseModel validationResponseModel = validator.validate(model, null);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(0, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
    }

    @Test
    void verifyEmptyConfig() {
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel();

        ValidationResponseModel validationResponseModel = validator.validate(model, null);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(4, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals(ConfigurationFieldValidator.REQUIRED_FIELD_MISSING_MESSAGE, status.getFieldMessage(), "Validation had unexpected field message.");
        }
    }

    @Test
    void verifyMalformedUrl() {
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        String badUrl = "notAValidUrl";
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            ID,
            NAME,
            CREATED_AT,
            LAST_UPDATED,
            badUrl,
            JiraServerAuthorizationMethod.BASIC,
            USER_NAME,
            PASSWORD,
            Boolean.FALSE,
            null,
            Boolean.FALSE,
            Boolean.FALSE
        );

        ValidationResponseModel validationResponseModel = validator.validate(model, null);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals("url", status.getFieldName(), "Validation reported an error for an unexpected field.");
        }
    }

    @Test
    void verifyPasswordIsSavedAndMissingFromModel() {
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            ID,
            NAME,
            CREATED_AT,
            LAST_UPDATED,
            URL,
            JiraServerAuthorizationMethod.BASIC,
            USER_NAME,
            null,
            Boolean.TRUE,
            null,
            Boolean.FALSE,
            Boolean.FALSE
        );

        ValidationResponseModel validationResponseModel = validator.validate(model, null);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(0, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
    }

    @Test
    void verifyPasswordIsMissingAndNotSaved() {
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(Mockito.anyString())).thenReturn(Optional.empty());
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(ID, NAME, URL, JiraServerAuthorizationMethod.BASIC);
        model.setUserName(USER_NAME);

        ValidationResponseModel validationResponseModel = validator.validate(model, null);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals("password", status.getFieldName(), "Validation reported an error for an unexpected field.");
        }
    }

    @Test
    void nameNotUniqueInvalidTest() {
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        JiraServerGlobalConfigModel existingModel = new JiraServerGlobalConfigModel(
            UUID.randomUUID().toString(),
            NAME,
            CREATED_AT,
            LAST_UPDATED,
            URL,
            JiraServerAuthorizationMethod.BASIC,
            USER_NAME,
            PASSWORD,
            Boolean.FALSE,
            null,
            Boolean.FALSE,
            Boolean.FALSE
        );
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(NAME)).thenReturn(Optional.of(existingModel));
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            null,
            NAME,
            CREATED_AT,
            LAST_UPDATED,
            URL,
            JiraServerAuthorizationMethod.BASIC,
            USER_NAME,
            PASSWORD,
            Boolean.FALSE,
            null,
            Boolean.FALSE,
            Boolean.FALSE
        );

        ValidationResponseModel validationResponseModel = validator.validate(model, null);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(1, alertFieldStatuses.size(), "There were errors in the configuration when none were expected.");
        for (AlertFieldStatus status : alertFieldStatuses) {
            assertEquals("name", status.getFieldName(), "Validation reported an error for an unexpected field.");
        }
    }

    @Test
    void nameNotUniqueButUpdatingValidTest() {
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        JiraServerGlobalConfigModel existingModel = new JiraServerGlobalConfigModel(
            ID,
            NAME,
            CREATED_AT,
            LAST_UPDATED,
            URL,
            JiraServerAuthorizationMethod.BASIC,
            USER_NAME,
            PASSWORD,
            Boolean.FALSE,
            null,
            Boolean.FALSE,
            Boolean.FALSE
        );
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(NAME)).thenReturn(Optional.of(existingModel));
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            ID,
            NAME,
            CREATED_AT,
            LAST_UPDATED,
            URL,
            JiraServerAuthorizationMethod.BASIC,
            "new" + USER_NAME,
            PASSWORD,
            Boolean.FALSE,
            null,
            Boolean.FALSE,
            Boolean.FALSE
        );

        ValidationResponseModel validationResponseModel = validator.validate(model, ID);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(0, alertFieldStatuses.size(), "Did not expect to find any errors.");
    }

    @Test
    void nameIsUniqueValidTest() {
        JiraServerGlobalConfigAccessor jiraServerGlobalConfigAccessor = Mockito.mock(JiraServerGlobalConfigAccessor.class);
        JiraServerGlobalConfigModel existingModel = new JiraServerGlobalConfigModel(
            ID,
            NAME,
            CREATED_AT,
            LAST_UPDATED,
            URL,
            JiraServerAuthorizationMethod.BASIC,
            USER_NAME,
            PASSWORD,
            Boolean.FALSE,
            null,
            Boolean.FALSE,
            Boolean.FALSE
        );
        Mockito.when(jiraServerGlobalConfigAccessor.getConfigurationByName(NAME)).thenReturn(Optional.of(existingModel));
        JiraServerGlobalConfigurationValidator validator = new JiraServerGlobalConfigurationValidator(jiraServerGlobalConfigAccessor);
        JiraServerGlobalConfigModel model = new JiraServerGlobalConfigModel(
            null,
            "new" + NAME,
            CREATED_AT,
            LAST_UPDATED,
            URL,
            JiraServerAuthorizationMethod.BASIC,
            USER_NAME,
            PASSWORD,
            Boolean.FALSE,
            null,
            Boolean.FALSE,
            Boolean.FALSE
        );

        ValidationResponseModel validationResponseModel = validator.validate(model, null);
        Collection<AlertFieldStatus> alertFieldStatuses = validationResponseModel.getErrors().values();
        assertEquals(0, alertFieldStatuses.size(), "Did not expect to find any errors.");
    }

}
