package com.synopsys.integration.alert.workflow.filter.builder;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.field.HierarchicalField;
import com.synopsys.integration.alert.database.entity.NotificationContent;
import com.synopsys.integration.alert.workflow.filter.JsonExtractor;

public class GsonFilterTest {
    public static final String PROJECT_NAME_1 = "Test Project 1";
    public static final String COMPONENT_NAME_1 = "Test Component 1";

    private JsonExtractor jsonExtractor;

    @Before
    public void init() {
        jsonExtractor = new JsonExtractor(new Gson());
    }

    @Test
    public void testSingleFilter() {
        final NotificationContent policyNotification = createPolicyContent(PROJECT_NAME_1, "1.0.0");
        final NotificationContent vulnerabilityNotification = createVulnerabilityContent("TestProject2", "1.0.0");
        final List<NotificationContent> notificationList = Arrays.asList(policyNotification, vulnerabilityNotification);

        final List<String> jsonFieldList = Arrays.asList("content");
        final JsonFieldFilterBuilder projectNameFilter = new JsonFieldFilterBuilder(jsonExtractor, new HierarchicalField(jsonFieldList, "projectName", null, null), PROJECT_NAME_1);

        final List<NotificationContent> filteredList = notificationList.stream().filter(projectNameFilter.buildPredicate()).collect(Collectors.toList());
        assertEquals(1, filteredList.size());
    }

    @Test
    public void testOrFilter() {
        final NotificationContent policyNotification = createPolicyContent(PROJECT_NAME_1, "1.0.0");
        final NotificationContent vulnerabilityNotification = createVulnerabilityContent(PROJECT_NAME_1, "1.0.0");
        final List<NotificationContent> notificationList = Arrays.asList(policyNotification, vulnerabilityNotification);

        final List<String> policyJsonFieldHierarchy = Arrays.asList("content");
        final List<String> vulnerabilityJsonFieldHierarchy = Arrays.asList("content", "affectedProjectVersions");
        final JsonFieldFilterBuilder policyProjectNameFilter = new JsonFieldFilterBuilder(jsonExtractor, new HierarchicalField(policyJsonFieldHierarchy, "projectName", null, null), PROJECT_NAME_1);
        final JsonFieldFilterBuilder vulnerabilityProjectNameFilter = new JsonFieldFilterBuilder(jsonExtractor, new HierarchicalField(vulnerabilityJsonFieldHierarchy, "projectName", null, null), PROJECT_NAME_1);
        final JsonFilterBuilder compoundFilter = new OrFieldFilterBuilder(policyProjectNameFilter, vulnerabilityProjectNameFilter);

        final List<NotificationContent> filteredList = notificationList.stream().filter(compoundFilter.buildPredicate()).collect(Collectors.toList());
        assertEquals(2, filteredList.size());
    }

    @Test
    public void testAndFilter() {
        final NotificationContent policyNotification = createPolicyContent(PROJECT_NAME_1, "1.0.0", COMPONENT_NAME_1, "1.2.1");
        final NotificationContent vulnerabilityNotification = createVulnerabilityContent(PROJECT_NAME_1, "1.0.0", COMPONENT_NAME_1, "1.2.1");
        final List<NotificationContent> notificationList = Arrays.asList(policyNotification, vulnerabilityNotification);

        final List<String> vulnerabilityComponentJsonFieldHierarchy = Arrays.asList("content");
        final List<String> vulnerabilityProjectJsonFieldHierarchy = Arrays.asList("content", "affectedProjectVersions");

        final JsonFieldFilterBuilder vulnerabilityComponentNameFilter = new JsonFieldFilterBuilder(jsonExtractor, new HierarchicalField(vulnerabilityComponentJsonFieldHierarchy, "componentName", null, null), COMPONENT_NAME_1);
        final JsonFieldFilterBuilder vulnerabilityProjectNameFilter = new JsonFieldFilterBuilder(jsonExtractor, new HierarchicalField(vulnerabilityProjectJsonFieldHierarchy, "projectName", null, null), PROJECT_NAME_1);
        final JsonFilterBuilder compoundFilter = new AndFieldFilterBuilder(vulnerabilityComponentNameFilter, vulnerabilityProjectNameFilter);

        final List<NotificationContent> filteredList = notificationList.stream().filter(compoundFilter.buildPredicate()).collect(Collectors.toList());
        assertEquals(1, filteredList.size());
    }

    private NotificationContent createPolicyContent(final String projectName, final String projectVersionName) {
        return createPolicyContent(projectName, projectVersionName, "empty", "empty");
    }

    private NotificationContent createPolicyContent(final String projectName, final String projectVersionName, final String componentName, final String componentVersionName) {
        final String content = "{\"content\":{\"projectName\":\""
                                   + projectName
                                   + "\",\"projectVersionName\":\""
                                   + projectVersionName
                                   + "\",\"componentVersionStatuses\":[{\"componentName\":\""
                                   + componentName
                                   + "\",\"componentVersionName\":\""
                                   + componentVersionName
                                   + "\",\"componentVersion\":\"https://int-hub04.dc1.lan/api/components/18dbecb7-a3b5-418b-9af1-44bf61ae0319/versions/3ef95202-5b60-4a62-ab07-02740212fd96\",\"bomComponentVersionPolicyStatus\":\"https://int-hub04.dc1.lan/api/projects/e8b0220a-3c39-4cf8-a8f2-85e2bec950a9/versions/ce613509-19da-41f9-99cb-7f4a82dfd3f1/components/18dbecb7-a3b5-418b-9af1-44bf61ae0319/versions/3ef95202-5b60-4a62-ab07-02740212fd96/policy-status\",\"componentIssueLink\":\"https://int-hub04.dc1.lan/api/projects/e8b0220a-3c39-4cf8-a8f2-85e2bec950a9/versions/ce613509-19da-41f9-99cb-7f4a82dfd3f1/components/18dbecb7-a3b5-418b-9af1-44bf61ae0319/component-versions/3ef95202-5b60-4a62-ab07-02740212fd96/issues\",\"policies\":[\"https://int-hub04.dc1.lan/api/policy-rules/9f34466a-a088-45a6-8048-35f96fcb989f\",\"https://int-hub04.dc1.lan/api/policy-rules/fedc4472-34b4-4c12-88bb-9fa63bae40a3\",\"https://int-hub04.dc1.lan/api/policy-rules/0b602e42-e339-46ab-8061-b2219088f233\"]}],\"policyInfos\":[{\"policyName\":\"No Apache Commons File Upload\",\"policy\":\"https://int-hub04.dc1.lan/api/policy-rules/9f34466a-a088-45a6-8048-35f96fcb989f\"},{\"policyName\":\"No Apache Commons File Upload 1.2.1\",\"policy\":\"https://int-hub04.dc1.lan/api/policy-rules/0b602e42-e339-46ab-8061-b2219088f233\"},{\"policyName\":\"No Commons FileUpload 1.2.1\",\"policy\":\"https://int-hub04.dc1.lan/api/policy-rules/fedc4472-34b4-4c12-88bb-9fa63bae40a3\"}],\"componentVersionsInViolation\":1,\"projectVersion\":\"https://int-hub04.dc1.lan/api/projects/e8b0220a-3c39-4cf8-a8f2-85e2bec950a9/versions/ce613509-19da-41f9-99cb-7f4a82dfd3f1\"},\"contentType\":\"application/json\",\"type\":\"RULE_VIOLATION\",\"createdAt\":\"2018-08-22T14:44:34.340Z\",\"_meta\":{\"allow\":[\"GET\"],\"href\":\"https://int-hub04.dc1.lan/api/notifications/f1224269-6b74-4263-8b6c-cb4de6be9d6c\"}}";
        return new NotificationContent(new Date(), "provider_blackduck", "RULE_VIOLATION", content);
    }

    private NotificationContent createVulnerabilityContent(final String projectName, final String projectVersion) {
        return createVulnerabilityContent(projectName, projectVersion, "empty", "empty");
    }

    private NotificationContent createVulnerabilityContent(final String projectName, final String projectVersion, final String componentName, final String componentVersionName) {
        final String content =
            "\t{\"content\":{\"componentVersionOriginName\":\"apache_software\",\"componentVersionOriginId\":\"commons-fileupload/commons-fileupload-1.2.1\",\"newVulnerabilityCount\":5,\"newVulnerabilityIds\":[{\"source\":\"NVD\",\"vulnerabilityId\":\"CVE-2016-1000031\",\"vulnerability\":\"https://int-hub04.dc1.lan/api/vulnerabilities/CVE-2016-1000031\"},{\"source\":\"BDSA\",\"vulnerabilityId\":\"BDSA-2013-0013\",\"relatedVulnerabilityId\":\"CVE-2013-2186\",\"vulnerability\":\"https://int-hub04.dc1.lan/api/vulnerabilities/BDSA-2013-0013\",\"relatedVulnerability\":\"https://int-hub04.dc1.lan/api/vulnerabilities/CVE-2013-2186\"},{\"source\":\"NVD\",\"vulnerabilityId\":\"CVE-2016-3092\",\"vulnerability\":\"https://int-hub04.dc1.lan/api/vulnerabilities/CVE-2016-3092\"},{\"source\":\"NVD\",\"vulnerabilityId\":\"CVE-2014-0050\",\"vulnerability\":\"https://int-hub04.dc1.lan/api/vulnerabilities/CVE-2014-0050\"},{\"source\":\"BDSA\",\"vulnerabilityId\":\"BDSA-2013-0001\",\"relatedVulnerabilityId\":\"CVE-2013-0248\",\"vulnerability\":\"https://int-hub04.dc1.lan/api/vulnerabilities/BDSA-2013-0001\",\"relatedVulnerability\":\"https://int-hub04.dc1.lan/api/vulnerabilities/CVE-2013-0248\"}],\"updatedVulnerabilityIds\":[],\"deletedVulnerabilityIds\":[],\"updatedVulnerabilityCount\":0,\"deletedVulnerabilityCount\":0,\"componentVersion\":\"https://int-hub04.dc1.lan/api/components/18dbecb7-a3b5-418b-9af1-44bf61ae0319/versions/3ef95202-5b60-4a62-ab07-02740212fd96\",\"componentName\":\""
                + componentName + "\",\"versionName\":\""
                + componentVersionName
                + "\",\"affectedProjectVersions\":"
                + "[{\"projectName\":\""
                + projectName
                + "\",\"projectVersionName\":\""
                + projectVersion
                + "\",\"projectVersion\":\"https://int-hub04.dc1.lan/api/projects/e8b0220a-3c39-4cf8-a8f2-85e2bec950a9/versions/ce613509-19da-41f9-99cb-7f4a82dfd3f1\",\"componentIssueUrl\":\"https://int-hub04.dc1.lan/api/projects/e8b0220a-3c39-4cf8-a8f2-85e2bec950a9/versions/ce613509-19da-41f9-99cb-7f4a82dfd3f1/components/18dbecb7-a3b5-418b-9af1-44bf61ae0319/component-versions/3ef95202-5b60-4a62-ab07-02740212fd96/issues\"}]},\"contentType\":\"application/json\",\"type\":\"VULNERABILITY\",\"createdAt\":\"2018-08-22T14:45:01.885Z\",\"_meta\":{\"allow\":[\"GET\"],\"href\":\"https://int-hub04.dc1.lan/api/notifications/f795205f-2509-4e3d-a65f-60acd9a46391\"}}";
        return new NotificationContent(new Date(), "provider_blackduck", "VULNERABILITY", content);
    }
}
