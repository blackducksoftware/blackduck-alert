package com.blackducksoftware.integration.hub.alert.datasource.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.blackducksoftware.integration.hub.alert.mock.entity.MockCommonDistributionEntity;

public class CommonDistributionConfigEntityTest extends EntityTest<CommonDistributionConfigEntity> {

    @Override
    public MockCommonDistributionEntity getMockUtil() {
        return new MockCommonDistributionEntity();
    }

    @Override
    public Class<CommonDistributionConfigEntity> getEntityClass() {
        return CommonDistributionConfigEntity.class;
    }

    @Override
    public void assertEntityFieldsNull(final CommonDistributionConfigEntity entity) {
        assertNull(entity.getDistributionConfigId());
        assertNull(entity.getDistributionType());
        assertNull(entity.getFilterByProject());
        assertNull(entity.getFrequency());
        assertNull(entity.getName());
    }

    @Override
    public void assertEntityFieldsFull(final CommonDistributionConfigEntity entity) {
        assertEquals(getMockUtil().getDistributionConfigId(), entity.getDistributionConfigId());
        assertEquals(getMockUtil().getDistributionType(), entity.getDistributionType());
        assertEquals(getMockUtil().getFilterByProject(), entity.getFilterByProject());
        assertEquals(getMockUtil().getFrequency(), entity.getFrequency());
        assertEquals(getMockUtil().getName(), entity.getName());
    }

    @Test
    @Override
    public void testEntity() throws JSONException {
        final CommonDistributionConfigEntity configEntity = getMockUtil().createEntity();

        assertEntityFieldsFull(configEntity);
        assertEquals(Long.valueOf(getMockUtil().getId()), configEntity.getId());

        final String expectedString = getMockUtil().getEntityJson();
        JSONAssert.assertEquals(expectedString, configEntity.toString(), false);

        final CommonDistributionConfigEntity configEntityNew = getMockUtil().createEntity();
        assertEquals(configEntity, configEntityNew);
    }

}
