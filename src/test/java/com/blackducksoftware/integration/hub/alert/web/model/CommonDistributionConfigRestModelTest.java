package com.blackducksoftware.integration.hub.alert.web.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.blackducksoftware.integration.hub.alert.mock.model.MockCommonDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

public class CommonDistributionConfigRestModelTest extends CommonDistributionRestModelTest<CommonDistributionConfigRestModel> {

    @Override
    public MockCommonDistributionRestModel getMockUtil() {
        final MockCommonDistributionRestModel mockCommonDistributionRestModel = new MockCommonDistributionRestModel();
        mockCommonDistributionRestModel.setId("1");
        return mockCommonDistributionRestModel;
    }

    @Override
    public Class<CommonDistributionConfigRestModel> getRestModelClass() {
        return CommonDistributionConfigRestModel.class;
    }

    @Override
    public void assertRestModelFieldsNull(final CommonDistributionConfigRestModel restModel) {
        assertNull(restModel.getDistributionConfigId());
        assertNull(restModel.getDistributionType());
        assertNull(restModel.getFilterByProject());
        assertNull(restModel.getFrequency());
        assertNull(restModel.getName());
        assertNull(restModel.getConfiguredProjects());
        assertNull(restModel.getLastRan());
        assertNull(restModel.getNotificationTypes());
        assertNull(restModel.getStatus());
    }

    @Override
    public void assertRestModelFieldsFull(final CommonDistributionConfigRestModel restModel) {
        assertEquals(getMockUtil().getDistributionConfigId(), restModel.getDistributionConfigId());
        assertEquals(getMockUtil().getDistributionType(), restModel.getDistributionType());
        assertEquals(getMockUtil().getFilterByProject(), restModel.getFilterByProject());
        assertEquals(getMockUtil().getFrequency(), restModel.getFrequency());
        assertEquals(getMockUtil().getName(), restModel.getName());
        assertEquals(getMockUtil().getLastRan(), restModel.getLastRan());
        assertEquals(getMockUtil().getStatus(), restModel.getStatus());
        assertEquals(getMockUtil().getNotificationsAsStrings(), restModel.getNotificationTypes());
        assertEquals(getMockUtil().getProjects(), restModel.getConfiguredProjects());
    }

}
