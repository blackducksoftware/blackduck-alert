package com.blackducksoftware.integration.alert.datasource.entity.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.blackducksoftware.integration.alert.datasource.relation.key.DistributionNotificationTypeRelationPK;

public class DistributionNotificationTypeRelationPKTest {

    @Test
    public void testGetAndSetMethods() {
        final DistributionNotificationTypeRelationPK primaryKey = new DistributionNotificationTypeRelationPK();
        final Long commonDistributionConfigId = 1L;
        final Long notificationTypeId = 2L;
        primaryKey.setCommonDistributionConfigId(commonDistributionConfigId);
        primaryKey.setNotificationTypeId(notificationTypeId);

        assertEquals(commonDistributionConfigId, primaryKey.getCommonDistributionConfigId());
        assertEquals(notificationTypeId, primaryKey.getNotificationTypeId());
    }
}
