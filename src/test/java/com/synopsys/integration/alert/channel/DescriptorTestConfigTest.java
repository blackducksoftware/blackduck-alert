package com.synopsys.integration.alert.channel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.gson.Gson;
import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.channel.event.ChannelEventProducer;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.DescriptorActionApi;
import com.synopsys.integration.alert.common.enumeration.ActionApiType;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.channel.DistributionChannelConfigEntity;
import com.synopsys.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.alert.web.model.TestConfigModel;
import com.synopsys.integration.exception.IntegrationException;

public abstract class DescriptorTestConfigTest<R extends CommonDistributionConfig, E extends DistributionChannelConfigEntity, GE extends GlobalChannelConfigEntity, EP extends ChannelEventProducer> extends AlertIntegrationTest {
    protected Gson gson;
    protected EP channelEventProducer;
    protected ContentConverter contentConverter;
    protected TestProperties properties;

    public abstract EP createChannelEventProducer();

    @Before
    public void init() {
        gson = new Gson();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
        properties = new TestProperties();
        channelEventProducer = createChannelEventProducer();
        cleanGlobalRepository();
        cleanDistributionRepositories();
    }

    public abstract void cleanGlobalRepository();

    public abstract void cleanDistributionRepositories();

    public abstract void saveGlobalConfiguration();

    public abstract ChannelDescriptor getDescriptor();

    public abstract MockRestModelUtil<R> getMockRestModelUtil();

    public abstract DatabaseEntity getDistributionEntity();

    public abstract void testCreateChannelEvent() throws Exception;

    @Test
    public void testSendTestMessage() throws Exception {
        saveGlobalConfiguration();
        final DescriptorActionApi descriptorActionApi = getDescriptor().getRestApi(ActionApiType.CHANNEL_DISTRIBUTION_CONFIG);
        final DescriptorActionApi spyDescriptorConfig = Mockito.spy(descriptorActionApi);
        final Config restModel = getMockRestModelUtil().createRestModel();
        try {
            spyDescriptorConfig.testConfig(new TestConfigModel(restModel));
        } catch (final IntegrationException e) {
            e.printStackTrace();
            Assert.fail();
        }

        Mockito.verify(spyDescriptorConfig).testConfig(Mockito.any());
    }

}
