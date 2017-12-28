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
package com.blackducksoftware.integration.hub.alert.datasource.entity.distribution;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockNotificationEntity;

public class NotificationEntityTest extends EntityTest<NotificationEntity> {
    MockNotificationEntity mockUtil = new MockNotificationEntity();

    @Override
    public MockEntityUtil<NotificationEntity> getMockUtil() {
        return mockUtil;
    }

    @Override
    public Class<NotificationEntity> getEntityClass() {
        return NotificationEntity.class;
    }

    @Override
    public void assertEntityFieldsNull(final NotificationEntity entity) {
        assertNull(entity.getComponentName());
        assertNull(entity.getComponentVersion());
        assertNull(entity.getCreatedAt());
        assertNull(entity.getEventKey());
        assertNull(entity.getNotificationType());
        assertNull(entity.getPerson());
        assertNull(entity.getPolicyRuleName());
        assertNull(entity.getProjectName());
        assertNull(entity.getProjectUrl());
        assertNull(entity.getProjectVersion());
        assertNull(entity.getProjectVersionUrl());
        assertNull(entity.getVulnerabilityList());
    }

    @Override
    public long entitySerialId() {
        return NotificationEntity.getSerialversionuid();
    }

    @Override
    public int emptyEntityHashCode() {
        return -1862761851;
    }

    @Override
    public void assertEntityFieldsFull(final NotificationEntity entity) {
        assertEquals(mockUtil.getComponentName(), entity.getComponentName());
        assertEquals(mockUtil.getComponentVersion(), entity.getComponentVersion());
        assertEquals(mockUtil.getCreatedAt(), entity.getCreatedAt());
        assertEquals(mockUtil.getEventKey(), entity.getEventKey());
        assertEquals(mockUtil.getNotificationType(), entity.getNotificationType());
        assertEquals(mockUtil.getPerson(), entity.getPerson());
        assertEquals(mockUtil.getPolicyRuleName(), entity.getPolicyRuleName());
        assertEquals(mockUtil.getProjectName(), entity.getProjectName());
        assertEquals(mockUtil.getProjectUrl(), entity.getProjectUrl());
        assertEquals(mockUtil.getProjectVersion(), entity.getProjectVersion());
        assertEquals(mockUtil.getProjectVersionUrl(), entity.getProjectVersionUrl());
        assertEquals(mockUtil.getVulnerabilityList(), entity.getVulnerabilityList());
    }

    @Override
    public int entityHashCode() {
        return 600107216;
    }

}
