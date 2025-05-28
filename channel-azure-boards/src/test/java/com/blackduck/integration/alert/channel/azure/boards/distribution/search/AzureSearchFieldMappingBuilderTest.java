package com.blackduck.integration.alert.channel.azure.boards.distribution.search;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;

class AzureSearchFieldMappingBuilderTest {

    @Test
    void testBuild() {
        String subTopicKey = "subTopicKey";
        String componentKey = "componentKey";
        String subComponentKey = "subComponentKey";
        String additionalInfoKey = "additionalInfoKey";
        String categoryKey = "categoryKey";

        AzureSearchFieldMappingBuilder builder = AzureSearchFieldMappingBuilder.create()
                .addSubTopic(subTopicKey)
                .addComponentKey(componentKey)
                .addSubComponentKey(subComponentKey)
                .addAdditionalInfoKey(additionalInfoKey)
                .addCategoryKey(categoryKey);

        List<AzureSearchFieldMappingBuilder.ReferenceToValue> referenceList = builder.buildAsList();

        Assertions.assertEquals(5, referenceList.size());
        Assertions.assertTrue(keyAndValueFound(referenceList, AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, subTopicKey));
        Assertions.assertTrue(keyAndValueFound(referenceList, AzureCustomFieldManager.ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME, componentKey));
        Assertions.assertTrue(keyAndValueFound(referenceList, AzureCustomFieldManager.ALERT_SUB_COMPONENT_KEY_FIELD_REFERENCE_NAME, subComponentKey));
        Assertions.assertTrue(keyAndValueFound(referenceList, AzureCustomFieldManager.ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME, additionalInfoKey));
        Assertions.assertTrue(keyAndValueFound(referenceList, AzureCustomFieldManager.ALERT_CATEGORY_KEY_FIELD_REFERENCE_NAME, categoryKey));
    }

    @Test
    void testSubComponentRemovalBuild() {
        String subTopicKey = "subTopicKey";
        String componentKey = "componentKey";
        String subComponentKey = "subComponentKey";
        String additionalInfoKey = "additionalInfoKey";
        String categoryKey = "categoryKey";

        AzureSearchFieldMappingBuilder builder = AzureSearchFieldMappingBuilder.create()
                .addSubTopic(subTopicKey)
                .addComponentKey(componentKey)
                .addSubComponentKey(subComponentKey)
                .addAdditionalInfoKey(additionalInfoKey)
                .addCategoryKey(categoryKey)
                .removeSubComponentKey();

        List<AzureSearchFieldMappingBuilder.ReferenceToValue> referenceList = builder.buildAsList();

        Assertions.assertEquals(4, referenceList.size());
        Assertions.assertTrue(keyAndValueFound(referenceList, AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, subTopicKey));
        Assertions.assertTrue(keyAndValueFound(referenceList, AzureCustomFieldManager.ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME, componentKey));
        Assertions.assertFalse(keyAndValueFound(referenceList, AzureCustomFieldManager.ALERT_SUB_COMPONENT_KEY_FIELD_REFERENCE_NAME, subComponentKey));
        Assertions.assertTrue(keyAndValueFound(referenceList, AzureCustomFieldManager.ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME, additionalInfoKey));
        Assertions.assertTrue(keyAndValueFound(referenceList, AzureCustomFieldManager.ALERT_CATEGORY_KEY_FIELD_REFERENCE_NAME, categoryKey));
    }

    boolean keyAndValueFound(List<AzureSearchFieldMappingBuilder.ReferenceToValue> fieldList, String fieldKey, String fieldValue ) {
        Predicate<AzureSearchFieldMappingBuilder.ReferenceToValue> predicate = (referenceToValue -> referenceToValue.getReferenceKey().equals(fieldKey) && referenceToValue.getFieldValue().equals(fieldValue));
        List<AzureSearchFieldMappingBuilder.ReferenceToValue> filteredList = fieldList.stream().filter(predicate).toList();
        return filteredList.size() == 1;
    }
}
