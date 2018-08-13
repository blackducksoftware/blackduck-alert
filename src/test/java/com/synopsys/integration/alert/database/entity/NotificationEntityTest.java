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
package com.synopsys.integration.alert.database.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.synopsys.integration.alert.mock.entity.MockNotificationEntity;

public class NotificationEntityTest extends EntityTest<NotificationEntity> {

    @Override
    public MockNotificationEntity getMockUtil() {
        return new MockNotificationEntity();
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
        assertNull(entity.getPolicyRuleUser());
        assertNull(entity.getPolicyRuleName());
        assertNull(entity.getProjectName());
        assertNull(entity.getProjectUrl());
        assertNull(entity.getProjectVersion());
        assertNull(entity.getProjectVersionUrl());
    }

    @Override
    public void assertEntityFieldsFull(final NotificationEntity entity) {
        assertEquals(getMockUtil().getComponentName(), entity.getComponentName());
        assertEquals(getMockUtil().getComponentVersion(), entity.getComponentVersion());
        assertEquals(getMockUtil().getCreatedAt(), entity.getCreatedAt());
        assertEquals(getMockUtil().getEventKey(), entity.getEventKey());
        assertEquals(getMockUtil().getNotificationType(), entity.getNotificationType());
        assertEquals(getMockUtil().getPolicyRuleUser(), entity.getPolicyRuleUser());
        assertEquals(getMockUtil().getPolicyRuleName(), entity.getPolicyRuleName());
        assertEquals(getMockUtil().getProjectName(), entity.getProjectName());
        assertEquals(getMockUtil().getProjectUrl(), entity.getProjectUrl());
        assertEquals(getMockUtil().getProjectVersion(), entity.getProjectVersion());
        assertEquals(getMockUtil().getProjectVersionUrl(), entity.getProjectVersionUrl());
    }

}
