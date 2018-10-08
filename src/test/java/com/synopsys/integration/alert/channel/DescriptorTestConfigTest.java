package com.synopsys.integration.alert.channel;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.google.gson.Gson;
import com.synopsys.integration.alert.Application;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.channel.event.ChannelEventProducer;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.DescriptorActionApi;
import com.synopsys.integration.alert.common.enumeration.ActionApiType;
import com.synopsys.integration.alert.database.DatabaseDataSource;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.channel.DistributionChannelConfigEntity;
import com.synopsys.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.test.annotation.DatabaseConnectionTest;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public abstract class DescriptorTestConfigTest<R extends CommonDistributionConfig, E extends DistributionChannelConfigEntity, GE extends GlobalChannelConfigEntity, EP extends ChannelEventProducer> {
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
            spyDescriptorConfig.testConfig(restModel);
        } catch (final IntegrationException e) {
            Assert.fail();
        }

        Mockito.verify(spyDescriptorConfig).testConfig(Mockito.any());
    }

}
