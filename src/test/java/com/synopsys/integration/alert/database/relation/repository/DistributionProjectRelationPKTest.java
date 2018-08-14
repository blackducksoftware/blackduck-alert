package com.synopsys.integration.alert.database.relation.repository;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.synopsys.integration.alert.database.relation.key.DistributionProjectRelationPK;

public class DistributionProjectRelationPKTest {

    @Test
    public void testGetAndSetMethods() {
        final DistributionProjectRelationPK primaryKey = new DistributionProjectRelationPK();
        final Long commonDistributionConfigId = 1L;
        final Long projectId = 2L;
        primaryKey.setCommonDistributionConfigId(commonDistributionConfigId);
        primaryKey.setProjectId(projectId);

        assertEquals(commonDistributionConfigId, primaryKey.getCommonDistributionConfigId());
        assertEquals(projectId, primaryKey.getProjectId());
    }
}
