package com.synopsys.integration.alert.web.model;

import static org.junit.Assert.assertEquals;

import com.synopsys.integration.alert.common.rest.model.NotificationConfig;
import com.synopsys.integration.alert.mock.model.MockNotificationRestModel;

public class NotificationRestModelTest extends RestModelTest<NotificationConfig> {

    @Override
    public MockNotificationRestModel getMockUtil() {
        return new MockNotificationRestModel();
    }

    @Override
    public void assertRestModelFieldsFull(NotificationConfig restModel) {
        assertEquals(getMockUtil().getCreatedAt(), restModel.getCreatedAt());
        assertEquals(getMockUtil().getProvider(), restModel.getProvider());
        assertEquals(getMockUtil().getNotificationType(), restModel.getNotificationType());
        assertEquals(getMockUtil().getContent(), restModel.getContent());
    }

}
