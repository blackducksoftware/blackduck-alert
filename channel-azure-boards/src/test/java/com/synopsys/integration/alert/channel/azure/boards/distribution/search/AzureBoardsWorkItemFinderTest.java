package com.synopsys.integration.alert.channel.azure.boards.distribution.search;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.azure.boards.distribution.AzureBoardsIssueTrackerQueryManager;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQuery;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQueryWhereJunctionType;
import com.synopsys.integration.azure.boards.common.service.query.fluent.WorkItemQueryWhereOperator;

public class AzureBoardsWorkItemFinderTest {

    @Test
    public void verifyQueryWhereStatementExists() throws AlertException {
        AzureBoardsIssueTrackerQueryManager azureBoardsIssueTrackerQueryManager = Mockito.mock(AzureBoardsIssueTrackerQueryManager.class);
        ArgumentCaptor<WorkItemQuery> workItemQueryArgumentCaptor = ArgumentCaptor.forClass(WorkItemQuery.class);

        String teamProjectName = "team project name";
        AzureBoardsWorkItemFinder azureBoardsWorkItemFinder = new AzureBoardsWorkItemFinder(azureBoardsIssueTrackerQueryManager, teamProjectName);

        LinkableItem provider = new LinkableItem("providerLabel", "providerValue");
        LinkableItem project = new LinkableItem("projectLabel", "projectValue");

        String componentKey = "componentKey";
        String subTopicKey = "subTopicKey";
        String additionalInfoKey = "additionalInfoKey";
        AzureSearchFieldMappingBuilder azureSearchFieldMappingBuilder = AzureSearchFieldMappingBuilder.create()
            .addComponentKey(componentKey)
            .addSubTopic(subTopicKey)
            .addAdditionalInfoKey(additionalInfoKey);

        Mockito.doReturn(List.of()).when(azureBoardsIssueTrackerQueryManager).executeQueryAndRetrieveWorkItems(Mockito.any());

        azureBoardsWorkItemFinder.findWorkItems(provider, project, azureSearchFieldMappingBuilder);

        Mockito.verify(azureBoardsIssueTrackerQueryManager).executeQueryAndRetrieveWorkItems(workItemQueryArgumentCaptor.capture());
        WorkItemQuery workItemQuery = workItemQueryArgumentCaptor.getValue();
        assertNotNull(workItemQuery);
        assertFalse(workItemQuery.exceedsCharLimit());

        Map<String, String> whereClauseValues = extractValuesFromWhereClause(workItemQuery.rawQuery());
        assertTrue(whereClauseValues.size() > 3);

        assertQueryDataFound(whereClauseValues, AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, subTopicKey);
        assertQueryDataFound(whereClauseValues, AzureCustomFieldManager.ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME, additionalInfoKey);
        assertQueryDataFound(whereClauseValues, AzureCustomFieldManager.ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME, componentKey);
    }

    @Test
    public void verifyQueryWhereStatementTruncated() throws AlertException {
        AzureBoardsIssueTrackerQueryManager azureBoardsIssueTrackerQueryManager = Mockito.mock(AzureBoardsIssueTrackerQueryManager.class);
        ArgumentCaptor<WorkItemQuery> workItemQueryArgumentCaptor = ArgumentCaptor.forClass(WorkItemQuery.class);

        String teamProjectName = "team project name";
        AzureBoardsWorkItemFinder azureBoardsWorkItemFinder = new AzureBoardsWorkItemFinder(azureBoardsIssueTrackerQueryManager, teamProjectName);

        LinkableItem provider = new LinkableItem("providerLabel", "providerValue");
        LinkableItem project = new LinkableItem("projectLabel", "projectValue");

        String componentKey = StringUtils.repeat("componentKey", 100);
        String subTopicKey = "subTopicKey";
        String additionalInfoKey = "additionalInfoKey";
        AzureSearchFieldMappingBuilder azureSearchFieldMappingBuilder = AzureSearchFieldMappingBuilder.create()
            .addComponentKey(componentKey)
            .addSubTopic(subTopicKey)
            .addAdditionalInfoKey(additionalInfoKey);

        Mockito.doReturn(List.of()).when(azureBoardsIssueTrackerQueryManager).executeQueryAndRetrieveWorkItems(Mockito.any());

        azureBoardsWorkItemFinder.findWorkItems(provider, project, azureSearchFieldMappingBuilder);

        Mockito.verify(azureBoardsIssueTrackerQueryManager).executeQueryAndRetrieveWorkItems(workItemQueryArgumentCaptor.capture());
        WorkItemQuery workItemQuery = workItemQueryArgumentCaptor.getValue();
        assertNotNull(workItemQuery);
        assertFalse(workItemQuery.exceedsCharLimit());

        Map<String, String> whereClauseValues = extractValuesFromWhereClause(workItemQuery.rawQuery());
        assertTrue(whereClauseValues.size() > 3);

        assertQueryDataFound(whereClauseValues, AzureCustomFieldManager.ALERT_SUB_TOPIC_KEY_FIELD_REFERENCE_NAME, subTopicKey);
        assertQueryDataFound(whereClauseValues, AzureCustomFieldManager.ALERT_ADDITIONAL_INFO_KEY_FIELD_REFERENCE_NAME, additionalInfoKey);

        String component = whereClauseValues.get(AzureCustomFieldManager.ALERT_COMPONENT_KEY_FIELD_REFERENCE_NAME);
        assertNotNull(component);

        // This is Azure boards custom field size limit
        String truncatedComponent = StringUtils.truncate(componentKey, 256);
        assertEquals(truncatedComponent, component);
    }

    public void assertQueryDataFound(Map<String, String> whereClauseValues, String key, String expectedValue) {
        String foundItem = whereClauseValues.get(key);
        assertNotNull(foundItem);
        assertEquals(expectedValue, foundItem);
    }

    // Expects ANDs in the WHERE clause each using the = operator
    private Map<String, String> extractValuesFromWhereClause(String query) {
        String whereClause = StringUtils.substringAfter(query, "WHERE (");
        String cleanedWhereClause = StringUtils.substringBefore(whereClause, ") ORDER");
        String[] andsInWhereClause = StringUtils.splitByWholeSeparator(cleanedWhereClause, " " + WorkItemQueryWhereJunctionType.AND.name() + " ");
        return Arrays.stream(andsInWhereClause)
            .map(StringUtils::trim)
            .map(clause -> StringUtils.remove(clause, "["))
            .map(clause -> StringUtils.remove(clause, "]"))
            .map(clause -> StringUtils.remove(clause, "'"))
            .map(clause -> StringUtils.splitByWholeSeparator(clause, WorkItemQueryWhereOperator.EQ.getComparator()))
            .filter(splitClause -> splitClause.length == 2)
            .collect(Collectors.toMap(splitClause -> splitClause[0].trim(), splitClause -> splitClause[1].trim()));
    }
}
