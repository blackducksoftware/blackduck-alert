/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.datasource.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.junit.Test;

public class NotificationEntityTest {

    @Test
    public void testEmptyModel() {
        final NotificationEntity notificationEntity = new NotificationEntity();
        assertEquals(-1194014350183607831L, NotificationEntity.getSerialversionuid());

        assertNull(notificationEntity.getComponentName());
        assertNull(notificationEntity.getComponentVersion());
        assertNull(notificationEntity.getCreatedAt());
        assertNull(notificationEntity.getEventKey());
        assertNull(notificationEntity.getId());
        assertNull(notificationEntity.getNotificationType());
        assertNull(notificationEntity.getPerson());
        assertNull(notificationEntity.getPolicyRuleName());
        assertNull(notificationEntity.getProjectName());
        assertNull(notificationEntity.getProjectVersion());
        assertNull(notificationEntity.getVulnerabilityList());

        assertEquals(-1862761851, notificationEntity.hashCode());

        final String expectedString = "{\"eventKey\":null,\"createdAt\":null,\"notificationType\":null,\"projectName\":null,\"projectVersion\":null,\"componentName\":null,\"componentVersion\":null,\"policyRuleName\":null,\"person\":null,\"projectUrl\":null,\"projectVersionUrl\":null,\"vulnerabilityList\":null,\"id\":null}";
        assertEquals(expectedString, notificationEntity.toString());

        final NotificationEntity notificationEntityNew = new NotificationEntity();
        assertEquals(notificationEntity, notificationEntityNew);
    }

    @Test
    public void testModel() {
        final Long id = 123L;
        final String eventKey = "EventKey";
        final Date createdAt = new Date(System.currentTimeMillis());
        final String notificationType = "NotificationType";
        final String projectName = "ProjectName";
        final String projectUrl = "projectUrl";
        final String projectVersion = "ProjectVersion";
        final String projectVersionUrl = "projectVersionUrl";
        final String componentName = "ComponentName";
        final String componentVersion = "ComponentVersion";
        final String policyRuleName = "PolicyRuleName";
        final String person = "Person";

        final VulnerabilityEntity vulnerabilityEntity = new VulnerabilityEntity("VulnerabilityId", "VulnerabilityOperation");
        final List<VulnerabilityEntity> vulnerabilityList = new ArrayList<>();
        vulnerabilityList.add(vulnerabilityEntity);

        final NotificationEntity notificationEntity = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person,
                vulnerabilityList);
        notificationEntity.setId(id);

        assertEquals(componentName, notificationEntity.getComponentName());
        assertEquals(componentVersion, notificationEntity.getComponentVersion());
        assertEquals(createdAt, notificationEntity.getCreatedAt());
        assertEquals(eventKey, notificationEntity.getEventKey());
        assertEquals(id, notificationEntity.getId());
        assertEquals(notificationType, notificationEntity.getNotificationType());
        assertEquals(person, notificationEntity.getPerson());
        assertEquals(policyRuleName, notificationEntity.getPolicyRuleName());
        assertEquals(projectName, notificationEntity.getProjectName());
        assertEquals(projectUrl, notificationEntity.getProjectUrl());
        assertEquals(projectVersion, notificationEntity.getProjectVersion());
        assertEquals(projectVersionUrl, notificationEntity.getProjectVersionUrl());
        assertEquals(vulnerabilityList, notificationEntity.getVulnerabilityList());

        assertEquals(HashCodeBuilder.reflectionHashCode(notificationEntity), notificationEntity.hashCode());

        final String expectedString = "{\"eventKey\":\"EventKey\",\"createdAt\":\"" + createdAt.toString()
                + "\",\"notificationType\":\"NotificationType\",\"projectName\":\"ProjectName\",\"projectVersion\":\"ProjectVersion\",\"componentName\":\"ComponentName\",\"componentVersion\":\"ComponentVersion\",\"policyRuleName\":\"PolicyRuleName\",\"person\":\"Person\",\"projectUrl\":\"projectUrl\",\"projectVersionUrl\":\"projectVersionUrl\",\"vulnerabilityList\":[{\"vulnerabilityId\":\"VulnerabilityId\",\"operation\":\"VulnerabilityOperation\",\"id\":null}],\"id\":123}";
        assertEquals(expectedString, notificationEntity.toString());

        final NotificationEntity notificationEntityNew = new NotificationEntity(eventKey, createdAt, notificationType, projectName, projectUrl, projectVersion, projectVersionUrl, componentName, componentVersion, policyRuleName, person,
                vulnerabilityList);
        notificationEntityNew.setId(id);

        assertEquals(notificationEntity, notificationEntityNew);
    }
}
