package com.blackducksoftware.integration.alert.channel;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

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

import com.blackducksoftware.integration.alert.Application;
import com.blackducksoftware.integration.alert.TestProperties;
import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.channel.event.ChannelEventFactory;
import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.common.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.common.enumeration.DescriptorConfigType;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.blackducksoftware.integration.alert.database.DatabaseDataSource;
import com.blackducksoftware.integration.alert.database.entity.NotificationContent;
import com.blackducksoftware.integration.alert.database.entity.channel.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfig;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.google.gson.Gson;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DatabaseDataSource.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public abstract class DescriptorTestConfigTest<R extends CommonDistributionConfig, E extends DistributionChannelConfigEntity, GE extends GlobalChannelConfigEntity> {
    protected Gson gson;
    protected ContentConverter contentConverter;
    protected ChannelEventFactory channelManager;
    protected TestProperties properties;

    @Before
    public void init() {
        gson = new Gson();
        contentConverter = new ContentConverter(gson, new DefaultConversionService());
        properties = new TestProperties();
        channelManager = new ChannelEventFactory(contentConverter);
        cleanDistributionRepository();
        cleanGlobalRepository();
    }

    public abstract void cleanGlobalRepository();

    public abstract void saveGlobalConfiguration();

    public abstract void cleanDistributionRepository();

    public abstract ChannelDescriptor getDescriptor();

    public abstract MockEntityUtil<E> getMockEntityUtil();

    @Test
    public void testCreateChannelEvent() {
        final Collection<ProjectData> projectData = Arrays.asList(new ProjectData(DigestType.DAILY, "Test project", "1", Arrays.asList(), new HashMap<>()));
        final DigestModel digestModel = new DigestModel(projectData);
        final NotificationContent notificationContent = new NotificationContent(new Date(), "provider", "notificationType", contentConverter.getJsonString(digestModel));
        final ChannelEvent channelEvent = channelManager.createChannelEvent(1L, getDescriptor().getDestinationName(), notificationContent);

        assertEquals(Long.valueOf(1L), channelEvent.getCommonDistributionConfigId());
        assertEquals(36, channelEvent.getEventId().length());
        assertEquals(getDescriptor().getDestinationName(), channelEvent.getDestination());
    }

    @Test
    public void testSendTestMessage() throws IntegrationException {
        saveGlobalConfiguration();
        final DescriptorConfig descriptorConfig = getDescriptor().getConfig(DescriptorConfigType.CHANNEL_DISTRIBUTION_CONFIG);
        final DescriptorConfig spyDescriptorConfig = Mockito.spy(descriptorConfig);
        final E entity = getMockEntityUtil().createEntity();
        try {
            spyDescriptorConfig.testConfig(entity);
        } catch (final IntegrationException e) {
            Assert.fail();
        }

        Mockito.verify(spyDescriptorConfig).testConfig(Mockito.any());
    }

}
