package com.synopsys.integration.alert.api.channel.jira.distribution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.api.channel.jira.distribution.custom.JiraCustomFieldResolver;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.FieldStatusSeverity;
import com.synopsys.integration.alert.common.exception.AlertFieldException;
import com.synopsys.integration.alert.test.common.TestResourceUtils;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.jira.common.model.components.SchemaComponent;
import com.synopsys.integration.jira.common.model.response.CustomFieldCreationResponseModel;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;

class JiraErrorMessageUtilityTest {

    @Test
    void verifyReporterRestExceptionTest() throws IntegrationException, IOException {
        JiraCustomFieldResolver jiraCustomFieldResolver = new JiraCustomFieldResolver(() -> List.of());
        JiraErrorMessageUtility jiraErrorMessageUtility = new JiraErrorMessageUtility(new Gson(), jiraCustomFieldResolver);

        IntegrationRestException integrationRestException = createRestException(JsonTestResource.REPORTER_ERROR);
        String issueCreatorKey = "issueCreatorFieldKey";
        String issueCreatorEmail = "issueCreatorEmail";
        AlertException alertException = jiraErrorMessageUtility.improveRestException(integrationRestException, issueCreatorKey, issueCreatorEmail);

        assertTrue(alertException instanceof AlertFieldException);

        AlertFieldException alertFieldException = (AlertFieldException) alertException;
        List<AlertFieldStatus> fieldErrors = alertFieldException.getFieldErrors();

        assertEquals(1, fieldErrors.size());
        AlertFieldStatus alertFieldStatus = fieldErrors.get(0);

        assertEquals(issueCreatorKey, alertFieldStatus.getFieldName());
        assertEquals(FieldStatusSeverity.ERROR, alertFieldStatus.getSeverity());

        String fieldMessage = alertFieldStatus.getFieldMessage();
        assertTrue(fieldMessage.contains(issueCreatorEmail));
        assertTrue(fieldMessage.contains("It'sa me, Mario"));
        assertFalse(fieldMessage.contains(" | Details: "));
    }

    @Test
    void verifyFieldErrorsRestExceptionTest() throws IntegrationException, IOException {
        String customFieldKey = "customfield_1";
        String customFieldName = "Custom Field 1 Name";
        String nonExistentCustomName = "Field 33 Name";
        Map<String, String> fieldIdsToNames = Map.of(
            "customfield_6", "lol",
            "customfield_33", nonExistentCustomName,
            customFieldKey, customFieldName
        );
        JiraCustomFieldResolver jiraCustomFieldResolver = new JiraCustomFieldResolver(() -> createCustomFieldResponseModels(fieldIdsToNames));
        JiraErrorMessageUtility jiraErrorMessageUtility = new JiraErrorMessageUtility(new Gson(), jiraCustomFieldResolver);

        IntegrationRestException integrationRestException = createRestException(JsonTestResource.ERROR_MESSAGES_ERROR);
        String issueCreatorKey = "issueCreatorFieldKey";
        String issueCreatorEmail = "issueCreatorEmail";
        AlertException alertException = jiraErrorMessageUtility.improveRestException(integrationRestException, issueCreatorKey, issueCreatorEmail);
        String message = alertException.getMessage();

        assertTrue(message.contains(customFieldName));
        assertTrue(message.contains("This field should have had another value"));
        assertTrue(message.contains(customFieldKey));
        assertFalse(message.contains(nonExistentCustomName));
    }

    private List<CustomFieldCreationResponseModel> createCustomFieldResponseModels(Map<String, String> idTonames) {
        return idTonames.entrySet()
            .stream()
            .map(this::createCustomerFieldCreationResponseModel)
            .collect(Collectors.toList());
    }

    private CustomFieldCreationResponseModel createCustomerFieldCreationResponseModel(Map.Entry<String, String> idToName) {
        return new CustomFieldCreationResponseModel(
            idToName.getKey(),
            "key",
            idToName.getValue(),
            false,
            false,
            false,
            List.of(),
            new SchemaComponent(
                "type",
                "system",
                "items"
            )
        );
    }

    private IntegrationRestException createRestException(JsonTestResource testResource) throws IntegrationException, IOException {
        return new IntegrationRestException(
            HttpMethod.GET,
            new HttpUrl("https://google.com"),
            HttpStatus.SC_BAD_REQUEST,
            "Bad Request",
            readInTestJson(testResource),
            "Rest Exception thrown"
        );
    }

    private String readInTestJson(JsonTestResource testResource) throws IOException {
        return TestResourceUtils.readFileToString("jira_error_message/" + testResource.getFileName());
    }

    enum JsonTestResource {
        REPORTER_ERROR("JiraErrorMessage_ReporterError.json"),
        ERROR_MESSAGES_ERROR("JiraErrorMessage_ErrorMessages.json");

        private final String fileName;

        JsonTestResource(String fileName) {
            this.fileName = fileName;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
