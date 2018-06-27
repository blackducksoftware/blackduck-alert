package com.blackducksoftware.integration.hub.alert.channel.manager;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.Application;
import com.blackducksoftware.integration.hub.alert.TestProperties;
import com.blackducksoftware.integration.hub.alert.config.DataSourceConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;
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
    private final Logger logger = LoggerFactory.getLogger(getClass());
    protected Gson gson;
    protected AlertEventContentConverter contentConverter;
    protected ObjectTransformer objectTransformer;
    protected DistributionChannelManager channelManager;
    @Autowired
    protected List<ChannelDescriptor> channelDescriptorList;
    protected TestProperties properties;

    @Before
    public void init() {
        gson = new Gson();
        contentConverter = new AlertEventContentConverter(gson);
        objectTransformer = new ObjectTransformer();
        properties = new TestProperties();
        channelManager = new DistributionChannelManager(objectTransformer, contentConverter, channelDescriptorList);
        cleanDistributionRepository();
        cleanGlobalRepository();
    }

    public abstract void cleanGlobalRepository();

    public abstract void saveGlobalConfiguration();

    public abstract void cleanDistributionRepository();

    public abstract String getDestination();

    public abstract MockRestModelUtil<R> getMockRestModelUtil();

    @Test
    public void testCreateChannelEvent() {
        final Collection<ProjectData> projectData = Arrays.asList(new ProjectData(DigestTypeEnum.DAILY, "Test project", "1", Arrays.asList(), new HashMap<>()));
        final DigestModel digestModel = new DigestModel(projectData);
        final ChannelEvent channelEvent = channelManager.createChannelEvent(getDestination(), digestModel, 1L);

        assertEquals(Long.valueOf(1L), channelEvent.getCommonDistributionConfigId());
        assertEquals(36, channelEvent.getEventId().length());
        assertEquals(getDestination(), channelEvent.getDestination());
    }

    @Test
    public void testSendTestMessage() throws IntegrationException {
        saveGlobalConfiguration();
        final String actual = channelManager.sendTestMessage(getDestination(), getMockRestModelUtil().createRestModel());
        final String expected = "Successfully sent test message";

        assertEquals(expected, actual);
    }

    @Test
    public void testSendTestMessageMissingGlobalConfiguration() throws IntegrationException {
        final Optional<ChannelDescriptor> channelDescriptor = channelDescriptorList.stream().filter(descriptor -> {
            return descriptor.getDestinationName().equals(getDestination());
        }).findFirst();
        if (channelDescriptor.get().hasGlobalConfiguration()) {
            final String actual = channelManager.sendTestMessage(getDestination(), getMockRestModelUtil().createRestModel());
            final String expected = "ERROR: Missing global configuration!";

            assertEquals(expected, actual);
        }
    }

    @Test
    public void testSendTestMessageEmptyChannelDescriptorList() throws IntegrationException {
        final DistributionChannelManager distributionChannelManager = new DistributionChannelManager(objectTransformer, contentConverter, Collections.emptyList());
        final String actual = distributionChannelManager.sendTestMessage(getDestination(), getMockRestModelUtil().createRestModel());
        final String expected = "Could not find a channel to send the test message";

        assertEquals(expected, actual);
    }
}
