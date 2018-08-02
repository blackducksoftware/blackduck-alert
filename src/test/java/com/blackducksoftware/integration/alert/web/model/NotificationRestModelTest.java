package com.blackducksoftware.integration.alert.web.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.alert.mock.model.MockNotificationRestModel;

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
        assertNull(restModel.getCreatedAt());
        assertNull(restModel.getProvider());
        assertNull(restModel.getNotificationType());
        assertNull(restModel.getContent());

    }

    @Override
    public void assertRestModelFieldsFull(final NotificationConfig restModel) {
        assertEquals(getMockUtil().getCreatedAt(), restModel.getCreatedAt());
        assertEquals(getMockUtil().getProvider(), restModel.getProvider());
        assertEquals(getMockUtil().getNotificationType(), restModel.getNotificationType());
        assertEquals(getMockUtil().getContent(), restModel.getContent());
    }

}
