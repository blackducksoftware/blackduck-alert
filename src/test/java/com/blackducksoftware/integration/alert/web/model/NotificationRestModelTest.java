package com.blackducksoftware.integration.alert.web.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.alert.mock.model.MockNotificationRestModel;
import com.blackducksoftware.integration.alert.web.model.NotificationConfig;

public class NotificationRestModelTest extends RestModelTest<NotificationConfig> {

    @Override
    public MockNotificationRestModel getMockUtil() {
        return new MockNotificationRestModel();
    }

    @Override
    public Class<NotificationConfig> getRestModelClass() {
        return NotificationConfig.class;
    }

    @Override
    public void assertRestModelFieldsNull(final NotificationConfig restModel) {
        assertNull(restModel.getComponents());
        assertNull(restModel.getCreatedAt());
        assertNull(restModel.getEventKey());
        assertNull(restModel.getNotificationTypes());
        assertNull(restModel.getProjectName());
        assertNull(restModel.getProjectUrl());
        assertNull(restModel.getProjectVersion());
        assertNull(restModel.getProjectVersionUrl());
    }

    @Override
    public void assertRestModelFieldsFull(final NotificationConfig restModel) {
        assertEquals(getMockUtil().getComponents(), restModel.getComponents());
        assertEquals(getMockUtil().getCreatedAt(), restModel.getCreatedAt());
        assertEquals(getMockUtil().getEventKey(), restModel.getEventKey());
        assertEquals(getMockUtil().getNotificationTypes(), restModel.getNotificationTypes());
        assertEquals(getMockUtil().getProjectName(), restModel.getProjectName());
        assertEquals(getMockUtil().getProjectUrl(), restModel.getProjectUrl());
        assertEquals(getMockUtil().getProjectVersion(), restModel.getProjectVersion());
        assertEquals(getMockUtil().getProjectVersionUrl(), restModel.getProjectVersionUrl());
    }

}
