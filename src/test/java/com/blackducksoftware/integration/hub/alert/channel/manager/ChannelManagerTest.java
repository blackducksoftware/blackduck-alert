package com.blackducksoftware.integration.hub.alert.channel.manager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;
import com.google.gson.Gson;

public abstract class ChannelManagerTest<R extends CommonDistributionConfigRestModel, E extends DistributionChannelConfigEntity, GE extends GlobalChannelConfigEntity, CM extends DistributionChannelManager<GE, E, R>> {

    protected Gson gson;
    protected AlertEventContentConverter contentConverter;

    @Before
    public void init() {
        gson = new Gson();
        contentConverter = new AlertEventContentConverter(gson);
    }

    public abstract CM getChannelManager();

    @Test
    public void testIsApplicable() {
        final CM channelManager = getChannelManager();
        final boolean fail = channelManager.isApplicable("test");
        final boolean pass = channelManager.isApplicable(getSupportedChannelName());

        assertTrue(pass);
        assertTrue(!fail);
    }

    public abstract String getSupportedChannelName();

    @Test
    public void testCreateChannelEvent() {
        final CM channelManager = getChannelManager();
        final Collection<ProjectData> projectData = Arrays.asList(new ProjectData(DigestTypeEnum.DAILY, "Test project", "1", Arrays.asList(), new HashMap<>()));
        final DigestModel digestModel = new DigestModel(projectData);
        final ChannelEvent channelEvent = channelManager.createChannelEvent(digestModel, 1L);

        assertEquals(Long.valueOf(1L), channelEvent.getCommonDistributionConfigId());
        assertEquals(36, channelEvent.getEventId().length());
        assertEquals(channelTopic(), channelEvent.getDestination());
    }

    public abstract String channelTopic();

    @Test
    public void testSendTestMessage() throws IntegrationException {
        final CM manager = getChannelManager();
        final DistributionChannel<GE, E> channel = manager.getDistributionChannel();
        if (getMockGlobalEntityUtil() != null) {
            Mockito.when(channel.getGlobalConfigEntity()).thenReturn(getMockGlobalEntityUtil().createGlobalEntity());
        }
        Mockito.doNothing().when(manager.getDistributionChannel()).sendAuditedMessage(Mockito.any(), Mockito.any());

        final String actual = manager.sendTestMessage(getMockRestModelUtil().createRestModel());
        final String expected = "Successfully sent test message";

        assertEquals(expected, actual);
    }

    public abstract MockEntityUtil<E> getMockEntityUtil();

    public abstract MockGlobalEntityUtil<GE> getMockGlobalEntityUtil();

    public abstract MockRestModelUtil<R> getMockRestModelUtil();

}
