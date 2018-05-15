package com.blackducksoftware.integration.hub.alert;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.config.GlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockCommonDistributionEntity;
import com.blackducksoftware.integration.hub.service.PhoneHomeService;
import com.blackducksoftware.integration.phonehome.PhoneHomeRequestBody;

public class PhoneHomeTest {

    @Test
    public void testProductVersionNull() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        globalProperties.setProductVersionOverride(GlobalProperties.PRODUCT_VERSION_UNKNOWN);
        final PhoneHome phoneHome = new PhoneHome(globalProperties, null);
        final PhoneHomeRequestBody.Builder builder = phoneHome.createPhoneHomeBuilder(null);

        Assert.assertNull(builder);
    }

    @Test
    public void testProductVersion() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final PhoneHome phoneHome = new PhoneHome(globalProperties, null);
        final PhoneHomeService phoneHomeService = phoneHome.createPhoneHomeService();
        final PhoneHomeRequestBody.Builder builder = phoneHome.createPhoneHomeBuilder(phoneHomeService);

        Assert.assertNotNull(builder);
        Assert.assertEquals(globalProperties.getProductVersion(), builder.getArtifactVersion());
        Assert.assertEquals("blackduck-alert", builder.getArtifactId());
    }

    @Test
    public void testChannelMetaData() {
        final TestGlobalProperties globalProperties = new TestGlobalProperties();
        final CommonDistributionRepositoryWrapper commonDistributionRepositoryWrapper = Mockito.mock(CommonDistributionRepositoryWrapper.class);
        Mockito.when(commonDistributionRepositoryWrapper.findAll()).thenReturn(createConfigEntities());
        final PhoneHome phoneHome = new PhoneHome(globalProperties, commonDistributionRepositoryWrapper);
        final PhoneHomeService phoneHomeService = phoneHome.createPhoneHomeService();
        final PhoneHomeRequestBody.Builder builder = phoneHome.createPhoneHomeBuilder(phoneHomeService);

        phoneHome.addChannelMetaData(builder);
        final Map<String, String> metaData = builder.getMetaData();

        Assert.assertNull(metaData.get("channel." + SupportedChannels.EMAIL_GROUP));
        Assert.assertEquals(String.valueOf(2), metaData.get("channel." + SupportedChannels.HIPCHAT));
        Assert.assertEquals(String.valueOf(1), metaData.get("channel." + SupportedChannels.SLACK));
    }

    private List<CommonDistributionConfigEntity> createConfigEntities() {
        final MockCommonDistributionEntity mockCommonDistributionEntity = new MockCommonDistributionEntity();
        final CommonDistributionConfigEntity entity1 = mockCommonDistributionEntity.createEntity();
        final CommonDistributionConfigEntity entity2 = mockCommonDistributionEntity.createEntity();
        mockCommonDistributionEntity.setDistributionType(SupportedChannels.SLACK);
        final CommonDistributionConfigEntity entity3 = mockCommonDistributionEntity.createEntity();

        return Arrays.asList(entity1, entity2, entity3);
    }
}
