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
package com.blackducksoftware.integration.alert.digest.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;

import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.blackducksoftware.integration.alert.common.enumeration.VulnerabilityOperation;
import com.blackducksoftware.integration.alert.common.model.NotificationModel;
import com.blackducksoftware.integration.alert.database.entity.NotificationCategoryEnum;
import com.blackducksoftware.integration.alert.database.entity.NotificationEntity;
import com.blackducksoftware.integration.alert.database.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.throwaway.ItemTypeEnum;

public class ProjectDataFactoryTest {

    @Test
    public void createProjectDataCollectionTest() {
        final Collection<NotificationModel> notifications = createNotificationCollection();

        final ProjectDataFactory projectDataFactory = new ProjectDataFactory();
        final Collection<ProjectData> projectDataCollection = projectDataFactory.createProjectDataCollection(notifications);

        assertFieldsForMultiple(notifications, projectDataCollection, DigestType.REAL_TIME);
    }

    @Test
    public void createProjectDataCollectionWithDigestTypeTest() {
        final Collection<NotificationModel> notifications = createNotificationCollection();

        final ProjectDataFactory projectDataFactory = new ProjectDataFactory();
        final DigestType digestType = DigestType.DAILY;
        final Collection<ProjectData> projectDataCollection = projectDataFactory.createProjectDataCollection(notifications, digestType);

        assertFieldsForMultiple(notifications, projectDataCollection, digestType);
    }

    @Test
    public void createProjectDataTest() {
        final NotificationModel notification = createVulnerabilityNotification();

        final ProjectDataFactory projectDataFactory = new ProjectDataFactory();
        final ProjectData projectData = projectDataFactory.createProjectData(notification);

        assertFields(notification, projectData);
        assertEquals(DigestType.REAL_TIME, projectData.getDigestType());
    }

    @Test
    public void createProjectDataWithDigestTypeTest() {
        final NotificationModel notification = createPolicyNotification();

        final ProjectDataFactory projectDataFactory = new ProjectDataFactory();
        DigestType digestType = DigestType.DAILY;
        ProjectData projectData = projectDataFactory.createProjectData(notification, digestType);

        assertFields(notification, projectData);
        assertEquals(digestType, projectData.getDigestType());

        digestType = DigestType.REAL_TIME;
        projectData = projectDataFactory.createProjectData(notification, digestType);
        assertFields(notification, projectData);
        assertEquals(digestType, projectData.getDigestType());
    }

    private void assertFieldsForMultiple(final Collection<NotificationModel> notifications, final Collection<ProjectData> projectData, final DigestType digestType) {
        for (final NotificationModel notification : notifications) {
            final ProjectData projData = find(projectData, notification.getProjectName(), notification.getProjectVersion());
            assertFields(notification, projData);
            assertEquals(digestType, projData.getDigestType());
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
        assertEquals(notification.getPolicyRuleName(), itemData.getDataSet().get(ItemTypeEnum.RULE.name()));
        assertEquals(notification.getComponentName(), itemData.getDataSet().get(ItemTypeEnum.COMPONENT.name()));
        assertEquals(notification.getComponentVersion(), itemData.getDataSet().get(ItemTypeEnum.VERSION.name()));
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
