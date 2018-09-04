/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.common.digest.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;

import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.enumeration.VulnerabilityOperation;
import com.synopsys.integration.alert.common.model.NotificationModel;
import com.synopsys.integration.alert.database.entity.NotificationCategoryEnum;
import com.synopsys.integration.alert.database.entity.NotificationEntity;
import com.synopsys.integration.alert.database.entity.VulnerabilityEntity;

public class ProjectDataFactoryTest {

    @Test
    public void createProjectDataCollectionTest() {
        final Collection<NotificationModel> notifications = createNotificationCollection();

        final ProjectDataFactory projectDataFactory = new ProjectDataFactory();
        final Collection<ProjectData> projectDataCollection = projectDataFactory.createProjectDataCollection(notifications);

        assertFieldsForMultiple(notifications, projectDataCollection, FrequencyType.REAL_TIME);
    }

    @Test
    public void createProjectDataCollectionWithDigestTypeTest() {
        final Collection<NotificationModel> notifications = createNotificationCollection();

        final ProjectDataFactory projectDataFactory = new ProjectDataFactory();
        final FrequencyType frequencyType = FrequencyType.DAILY;
        final Collection<ProjectData> projectDataCollection = projectDataFactory.createProjectDataCollection(notifications, frequencyType);

        assertFieldsForMultiple(notifications, projectDataCollection, frequencyType);
    }

    @Test
    public void createProjectDataTest() {
        final NotificationModel notification = createVulnerabilityNotification();

        final ProjectDataFactory projectDataFactory = new ProjectDataFactory();
        final ProjectData projectData = projectDataFactory.createProjectData(notification);

        assertFields(notification, projectData);
        assertEquals(FrequencyType.REAL_TIME, projectData.getFrequencyType());
    }

    @Test
    public void createProjectDataWithDigestTypeTest() {
        final NotificationModel notification = createPolicyNotification();

        final ProjectDataFactory projectDataFactory = new ProjectDataFactory();
        FrequencyType frequencyType = FrequencyType.DAILY;
        ProjectData projectData = projectDataFactory.createProjectData(notification, frequencyType);

        assertFields(notification, projectData);
        assertEquals(frequencyType, projectData.getFrequencyType());

        frequencyType = FrequencyType.REAL_TIME;
        projectData = projectDataFactory.createProjectData(notification, frequencyType);
        assertFields(notification, projectData);
        assertEquals(frequencyType, projectData.getFrequencyType());
    }

    private void assertFieldsForMultiple(final Collection<NotificationModel> notifications, final Collection<ProjectData> projectData, final FrequencyType frequencyType) {
        for (final NotificationModel notification : notifications) {
            final ProjectData projData = find(projectData, notification.getProjectName(), notification.getProjectVersion());
            assertFields(notification, projData);
            assertEquals(frequencyType, projData.getFrequencyType());
        }
    }

    private ProjectData find(final Collection<ProjectData> projectDataCollection, final String projectName, final String projectVersion) {
        for (final ProjectData projectData : projectDataCollection) {
            if (projectData.getProjectName().equals(projectName) && projectData.getProjectVersion().equals(projectVersion)) {
                return projectData;
            }
        }
        return null;
    }

    private void assertFields(final NotificationModel notification, final ProjectData projectData) {
        assertEquals(notification.getProjectName() + notification.getProjectVersion(), projectData.getProjectKey());
        assertEquals(notification.getProjectName(), projectData.getProjectName());
        assertEquals(notification.getProjectVersion(), projectData.getProjectVersion());

        final CategoryData categoryData = projectData.getCategoryMap().get(notification.getNotificationType());
        final ItemData itemData = categoryData.getItems().iterator().next();
        assertEquals(notification.getNotificationType().name(), categoryData.getCategoryKey());
        assertEquals(notification.getPolicyRuleName(), itemData.getDataSet().get(ProjectData.DATASET_KEY_RULE));
        assertEquals(notification.getComponentName(), itemData.getDataSet().get(ProjectData.DATASET_KEY_COMPONENT));
        assertEquals(notification.getComponentVersion(), itemData.getDataSet().get(ProjectData.DATASET_KEY_VERSION));
    }

    private Collection<NotificationModel> createNotificationCollection() {
        final String differentProject = "Different Project";
        final String differentVersion = "Different Project Version";
        return Arrays.asList(createVulnerabilityNotification(), createPolicyNotification(), createVulnerabilityNotification(differentProject, differentVersion), createPolicyNotification(differentProject, differentVersion),
        createPolicyNotification("One More", "1.0.0"));
    }

    private NotificationModel createVulnerabilityNotification() {
        return createVulnerabilityNotification("Project Name", "Project Version");
    }

    private NotificationModel createVulnerabilityNotification(final String projectName, final String projectVersion) {
        final String eventKey = "key";
        final Date createdAt = new Date();
        final NotificationCategoryEnum notificationType = NotificationCategoryEnum.MEDIUM_VULNERABILITY;
        final String projectUrl = "http://localhost:8080";
        final String projectVersionUrl = "http://localhost:8080";
        final String componentName = "Component";
        final String componentVersion = "Component Version";
        final String policyRuleName = null;
        final String policyRuleUser = null;

        final String vulnerabilityId = NotificationCategoryEnum.VULNERABILITY.name();
        final VulnerabilityOperation vulnerabilityOperation = VulnerabilityOperation.ADD;
        final NotificationEntity notification = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, policyRuleUser);
        notification.setId(1L);
        final Collection<VulnerabilityEntity> vulnerabilityList = Arrays.asList(new VulnerabilityEntity(vulnerabilityId, vulnerabilityOperation, notification.getId()));

        return new NotificationModel(notification, vulnerabilityList);
    }

    private NotificationModel createPolicyNotification() {
        return createPolicyNotification("Project Name", "Project Version");
    }

    private NotificationModel createPolicyNotification(final String projectName, final String projectVersion) {
        final String eventKey = "key";
        final Date createdAt = new Date();
        final NotificationCategoryEnum notificationType = NotificationCategoryEnum.POLICY_VIOLATION;
        final String projectUrl = "http://localhost:8080";
        final String projectVersionUrl = "http://localhost:8080";
        final String componentName = "Other Component";
        final String componentVersion = "Other Component Version";
        final String policyRuleName = "Policy Rule Name";
        final String person = "Person";

        final String vulnerabilityId = NotificationCategoryEnum.VULNERABILITY.name();
        final VulnerabilityOperation vulnerabilityOperation = VulnerabilityOperation.ADD;
        final NotificationEntity notification = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person);
        notification.setId(1L);
        final Collection<VulnerabilityEntity> vulnerabilityList = Arrays.asList(new VulnerabilityEntity(vulnerabilityId, vulnerabilityOperation, notification.getId()));

        return new NotificationModel(notification, vulnerabilityList);
    }

}
