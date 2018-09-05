package com.synopsys.integration.alert.database.entity;

import org.junit.Assert;
import org.junit.Test;

import com.synopsys.integration.blackduck.api.generated.enumeration.NotificationType;

public class NotificationTypeEntityTest {

    @Test
    public void constructorTest() {
        final NotificationType type = NotificationType.VULNERABILITY;
        final NotificationTypeEntity entity = new NotificationTypeEntity(type);
        Assert.assertNotNull(entity);
    }

    @Test
    public void getTypeTest() {
        final NotificationType type = NotificationType.VULNERABILITY;
        final NotificationTypeEntity entity = new NotificationTypeEntity(type);
        Assert.assertEquals(type, entity.getType());
    }
}
