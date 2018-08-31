package com.synopsys.integration.alert.database.entity.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.synopsys.integration.alert.database.relation.key.DistributionNotificationTypeRelationPK;

public class DistributionNotificationTypeRelationPKTest {

    @Test
    public void testGetAndSetMethods() {
        final DistributionNotificationTypeRelationPK primaryKey = new DistributionNotificationTypeRelationPK();
        final Long commonDistributionConfigId = 1L;
        final String notificationType = "notification_type";
        primaryKey.setCommonDistributionConfigId(commonDistributionConfigId);
        primaryKey.setNotificationType(notificationType);

        assertEquals(commonDistributionConfigId, primaryKey.getCommonDistributionConfigId());
        assertEquals(notificationType, primaryKey.getNotificationType());
    }
}
