package com.blackducksoftware.integration.hub.alert.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.enumeration.VulnerabilityOperationEnum;
import com.blackducksoftware.integration.hub.alert.hub.model.NotificationModel;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;

public class NotificationModelTest {

    private NotificationEntity createNotificationEntity() {
        final String eventKey = "event_key_for_notification";
        final Date createdAt = Date.from(LocalDateTime.now().toInstant(ZoneOffset.UTC));
        final NotificationCategoryEnum notificationType = NotificationCategoryEnum.VULNERABILITY;
        final String projectName = "projectName";
        final String projectVersion = "projectVersion";
        final String componentName = "componentName";
        final String componentVersion = "componentVersion";
        final String policyRuleName = "policyRuleName";
        final String person = "person";
        final String projectUrl = "projectURL";
        final String projectVersionUrl = "projectVersionUrl";
        final NotificationEntity entity = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person);
        return entity;
    }

    @Test
    public void testModel() {
        final NotificationEntity entity = createNotificationEntity();
        final VulnerabilityEntity vuln1 = new VulnerabilityEntity("id1", VulnerabilityOperationEnum.ADD, 1L);
        final List<VulnerabilityEntity> vulnerabilityList = Arrays.asList(vuln1);
        final NotificationModel model = new NotificationModel(entity, vulnerabilityList);
        assertEquals(entity.getComponentName(), model.getComponentName());
        assertEquals(entity.getComponentVersion(), model.getComponentVersion());
        assertEquals(entity.getCreatedAt(), model.getCreatedAt());
        assertEquals(entity.getEventKey(), model.getEventKey());
        assertEquals(entity, model.getNotificationEntity());
        assertEquals(entity.getNotificationType(), model.getNotificationType());
        assertEquals(entity.getPolicyRuleName(), model.getPolicyRuleName());
        assertEquals(entity.getPolicyRuleUser(), model.getPolicyRuleUser());
        assertEquals(entity.getProjectName(), model.getProjectName());
        assertEquals(entity.getProjectUrl(), model.getProjectUrl());
        assertEquals(entity.getProjectVersion(), model.getProjectVersion());
        assertEquals(entity.getProjectVersionUrl(), model.getProjectVersionUrl());
        assertEquals(vulnerabilityList.size(), model.getVulnerabilityList().size());
        final boolean allEqual = model.getVulnerabilityList().stream().allMatch(vuln1::equals);
        assertTrue(allEqual);
    }

    @Test
    public void testNotificationNull() {
        final NotificationEntity entity = null;
        final VulnerabilityEntity vuln1 = new VulnerabilityEntity("id1", VulnerabilityOperationEnum.ADD, 1L);
        final List<VulnerabilityEntity> vulnerabilityList = Arrays.asList(vuln1);
        final NotificationModel model = new NotificationModel(entity, vulnerabilityList);
        assertNull(model.getComponentName());
        assertNull(model.getComponentVersion());
        assertNull(model.getCreatedAt());
        assertNull(model.getEventKey());
        assertNull(model.getNotificationEntity());
        assertNull(model.getNotificationType());
        assertNull(model.getPolicyRuleName());
        assertNull(model.getPolicyRuleUser());
        assertNull(model.getProjectName());
        assertNull(model.getProjectUrl());
        assertNull(model.getProjectVersion());
        assertNull(model.getProjectVersionUrl());
        assertEquals(vulnerabilityList.size(), model.getVulnerabilityList().size());
        final boolean allEqual = model.getVulnerabilityList().stream().allMatch(vuln1::equals);
        assertTrue(allEqual);
    }
}
