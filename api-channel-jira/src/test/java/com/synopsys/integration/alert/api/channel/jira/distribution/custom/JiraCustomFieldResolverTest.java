package com.synopsys.integration.alert.api.channel.jira.distribution.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.function.ThrowingSupplier;
import com.synopsys.integration.jira.common.model.components.SchemaComponent;
import com.synopsys.integration.jira.common.model.response.CustomFieldCreationResponseModel;

class JiraCustomFieldResolverTest {
    private static final String FIELD_ID_1 = "first_id";
    private static final String FIELD_NAME_1 = "first_field";
    private static final String FIELD_KEY = "key";
    private static final String FIELD_SCHEMA_TYPE = "type";

    @Test
    void verifyCachesAreInitializedTest() {
        JiraCustomFieldResolver jiraCustomFieldResolver = new JiraCustomFieldResolver(retrieveCustomFields(Map.of(
            FIELD_ID_1, FIELD_NAME_1
        )));

        Set<String> customFieldIds = jiraCustomFieldResolver.getCustomFieldIds();
        assertEquals(1, customFieldIds.size());

        Optional<CustomFieldCreationResponseModel> customFieldCreationResponseModel = jiraCustomFieldResolver.retrieveFieldDefinition(FIELD_NAME_1);
        assertTrue(customFieldCreationResponseModel.isPresent());

        CustomFieldCreationResponseModel customField = customFieldCreationResponseModel.get();
        assertEquals(FIELD_ID_1, customField.getId());
    }

    @Test
    void resolveCustomFieldTest() {
        JiraCustomFieldResolver jiraCustomFieldResolver = new JiraCustomFieldResolver(retrieveCustomFields(Map.of(
            FIELD_ID_1, FIELD_NAME_1
        )));

        Optional<CustomFieldCreationResponseModel> customFieldCreationResponseModel = jiraCustomFieldResolver.retrieveFieldDefinition(FIELD_NAME_1);
        assertTrue(customFieldCreationResponseModel.isPresent());

        CustomFieldCreationResponseModel customField = customFieldCreationResponseModel.get();
        assertEquals(FIELD_ID_1, customField.getId());
        assertEquals(FIELD_KEY, customField.getKey());
        assertEquals(FIELD_SCHEMA_TYPE, customField.getSchema().getType());
    }

    @Test
    void getCustomFieldsIdsTest() {
        String anotherId = "AnotherId";
        JiraCustomFieldResolver jiraCustomFieldResolver = new JiraCustomFieldResolver(retrieveCustomFields(Map.of(
            FIELD_ID_1, FIELD_NAME_1,
            anotherId, "AnotherFieldName"
        )));

        Set<String> customFieldIds = jiraCustomFieldResolver.getCustomFieldIds();
        assertEquals(2, customFieldIds.size());
        assertTrue(customFieldIds.containsAll(List.of(anotherId, FIELD_ID_1)));
        assertFalse(customFieldIds.contains(FIELD_NAME_1));
        assertFalse(customFieldIds.contains(FIELD_SCHEMA_TYPE));
    }

    @Test
    void resolveCustomFieldToIdTest() {
        String anotherId = "AnotherId";
        String anotherName = "AnotherFieldName";
        JiraCustomFieldResolver jiraCustomFieldResolver = new JiraCustomFieldResolver(retrieveCustomFields(Map.of(
            FIELD_ID_1, FIELD_NAME_1,
            anotherId, anotherName
        )));

        Optional<String> resolvedCustomFieldIdToName = jiraCustomFieldResolver.resolveCustomFieldIdToName(FIELD_ID_1);
        assertTrue(resolvedCustomFieldIdToName.isPresent());

        String name = resolvedCustomFieldIdToName.get();
        assertEquals(FIELD_NAME_1, name);

        Optional<String> anotherResolvedIdToName = jiraCustomFieldResolver.resolveCustomFieldIdToName(anotherId);
        assertTrue(anotherResolvedIdToName.isPresent());

        String anotherResolvedName = anotherResolvedIdToName.get();
        assertEquals(anotherName, anotherResolvedName);

        Optional<String> missingName = jiraCustomFieldResolver.resolveCustomFieldIdToName("doesn't exist");
        assertTrue(missingName.isEmpty());
    }

    private ThrowingSupplier<List<CustomFieldCreationResponseModel>, IntegrationException> retrieveCustomFields(Map<String, String> idToNames) {
        return () -> createCustomFieldResponseModels(idToNames);
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
            FIELD_KEY,
            idToName.getValue(),
            false,
            false,
            false,
            List.of(),
            new SchemaComponent(
                FIELD_SCHEMA_TYPE,
                "system",
                "items"
            )
        );
    }
}
