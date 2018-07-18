package com.blackducksoftware.integration.alert.channel;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
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
import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.ObjectTransformer;
import com.blackducksoftware.integration.alert.TestProperties;
import com.blackducksoftware.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.common.enumeration.DigestType;
import com.blackducksoftware.integration.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.alert.database.entity.channel.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.channel.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.alert.event.ChannelEvent;
import com.blackducksoftware.integration.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.test.annotation.DatabaseConnectionTest;
import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.google.gson.Gson;

@Category(DatabaseConnectionTest.class)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { Application.class, DataSourceConfig.class })
@TestPropertySource(locations = "classpath:spring-test.properties")
@Transactional
@WebAppConfiguration
@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class, TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
public abstract class ChannelManagerTest<R extends CommonDistributionConfigRestModel, E extends DistributionChannelConfigEntity, GE extends GlobalChannelConfigEntity> {
    protected Gson gson;
    protected ContentConverter contentConverter;
    protected ObjectTransformer objectTransformer;
    protected DistributionChannelManager channelManager;
    protected TestProperties properties;

    @Before
    public void init() {
        gson = new Gson();
        contentConverter = new ContentConverter(gson);
        objectTransformer = new ObjectTransformer();
        properties = new TestProperties();
        channelManager = new DistributionChannelManager(objectTransformer, contentConverter);
        cleanDistributionRepository();
        cleanGlobalRepository();
    }

    public abstract void cleanGlobalRepository();

    public abstract void saveGlobalConfiguration();

    public abstract void cleanDistributionRepository();

    public abstract ChannelDescriptor getDescriptor();

    public abstract MockRestModelUtil<R> getMockRestModelUtil();

    @Test
    public void testCreateChannelEvent() {
        final Collection<ProjectData> projectData = Arrays.asList(new ProjectData(DigestType.DAILY, "Test project", "1", Arrays.asList(), new HashMap<>()));
        final DigestModel digestModel = new DigestModel(projectData);
        final ChannelEvent channelEvent = channelManager.createChannelEvent(getDescriptor().getDestinationName(), digestModel, 1L);

        assertEquals(Long.valueOf(1L), channelEvent.getCommonDistributionConfigId());
        assertEquals(36, channelEvent.getEventId().length());
        assertEquals(getDescriptor().getDestinationName(), channelEvent.getDestination());
    }

    @Test
    public void testSendTestMessage() throws IntegrationException {
        saveGlobalConfiguration();
        final String actual = channelManager.sendTestMessage(getMockRestModelUtil().createRestModel(), getDescriptor());
        final String expected = "Successfully sent test message";

        assertEquals(expected, actual);
    }

    @Test
    public void testSendTestMessageMissingGlobalConfiguration() throws IntegrationException {
        if (getDescriptor().hasGlobalConfiguration()) {
            final String actual = channelManager.sendTestMessage(getMockRestModelUtil().createRestModel(), getDescriptor());
            final String expected = "ERROR: Missing global configuration!";

            assertEquals(expected, actual);
        }
    }
}
