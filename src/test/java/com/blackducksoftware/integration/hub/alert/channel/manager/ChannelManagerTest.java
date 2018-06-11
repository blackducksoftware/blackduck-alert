package com.blackducksoftware.integration.hub.alert.channel.manager;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.channel.ChannelDescriptor;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.DistributionChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalChannelConfigEntity;
import com.blackducksoftware.integration.hub.alert.event.AlertEventContentConverter;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.entity.global.MockGlobalEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;
import com.google.gson.Gson;

public abstract class ChannelManagerTest<R extends CommonDistributionConfigRestModel, E extends DistributionChannelConfigEntity, GE extends GlobalChannelConfigEntity> {

    protected Gson gson;
    protected AlertEventContentConverter contentConverter;
    protected DistributionChannelManager channelManager;

    @Before
    public void init() {
        gson = new Gson();
        contentConverter = new AlertEventContentConverter(gson);
        final List<ChannelDescriptor> channelDescriptorList = new ArrayList<>();
        // TODO populate descriptor
        //        final ChannelDescriptor email_descriptor = new ChannelDescriptor(EmailGroupChannel.COMPONENT_NAME, EmailGroupChannel.COMPONENT_NAME, new EmailGroupChannel(gson, , contentConverter), true));
        //        final ChannelDescriptor hipchat_descriptor = new ChannelDescriptor(HipChatChannel.COMPONENT_NAME, HipChatChannel.COMPONENT_NAME, distributionChannel, true));
        //        final ChannelDescriptor slack_descriptor = new ChannelDescriptor(SlackChannel.COMPONENT_NAME, SlackChannel.COMPONENT_NAME, distributionChannel, false));
        final ObjectTransformer objectTransformer = new ObjectTransformer();
        channelManager = new DistributionChannelManager(objectTransformer, contentConverter, channelDescriptorList);

    }

    public abstract String getDestination();

    @Test
    public void testCreateChannelEvent() {
        // TODO fix test
        //        final Collection<ProjectData> projectData = Arrays.asList(new ProjectData(DigestTypeEnum.DAILY, "Test project", "1", Arrays.asList(), new HashMap<>()));
        //        final DigestModel digestModel = new DigestModel(projectData);
        //        final ChannelEvent channelEvent = channelManager.createChannelEvent(digestModel, 1L);
        //
        //        assertEquals(Long.valueOf(1L), channelEvent.getCommonDistributionConfigId());
        //        assertEquals(36, channelEvent.getEventId().length());
        //        assertEquals(getDestination(), channelEvent.getDestination());
    }

    @Test
    public void testSendTestMessage() throws IntegrationException {
        //TODO fix test
        //        if (getMockGlobalEntityUtil() != null) {
        //            Mockito.when(channel.getGlobalConfigEntity()).thenReturn(getMockGlobalEntityUtil().createGlobalEntity());
        //        }
        //
        //        final String actual = channelManager.sendTestMessage(getDestination(), getMockRestModelUtil().createRestModel());
        //        final String expected = "Successfully sent test message";
        //
        //        assertEquals(expected, actual);
    }

    public abstract MockEntityUtil<E> getMockEntityUtil();

    public abstract MockGlobalEntityUtil<GE> getMockGlobalEntityUtil();

    public abstract MockRestModelUtil<R> getMockRestModelUtil();

}
