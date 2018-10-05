package com.synopsys.integration.alert.channel.hipchat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Collections;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.DescriptorTestConfigTest;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatEntity;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatRestModel;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.mock.model.MockRestModelUtil;
import com.synopsys.integration.alert.web.channel.model.HipChatDistributionConfig;

public class HipChatDescriptorTestIT extends DescriptorTestConfigTest<HipChatDistributionConfig, HipChatEventProducer> {
    @Autowired
    private HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor;
    @Autowired
    private HipChatGlobalRepositoryAccessor hipChatRepository;
    @Autowired
    private HipChatDescriptor hipChatDescriptor;

    @Override
    @Test
    public void testCreateChannelEvent() {
        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());
        final DatabaseEntity distributionEntity = getDistributionEntity();
        final String roomId = "12345";
        final String color = "purple";
        final HipChatDistributionConfig jobConfig = new HipChatDistributionConfig("1", roomId, false, color, String.valueOf(distributionEntity.getId()), getDescriptor().getDestinationName(), "Test Job", "provider",
            FrequencyType.DAILY.name(), "true",
            Collections.emptyList(), Collections.emptyList(), FormatType.DIGEST.name());

        final HipChatChannelEvent channelEvent = channelEventProducer.createChannelEvent(jobConfig, content);

        assertEquals(Long.valueOf(1L), channelEvent.getCommonDistributionConfigId());
        assertEquals(36, channelEvent.getEventId().length());
        assertEquals(getDescriptor().getDestinationName(), channelEvent.getDestination());
        assertEquals(roomId, String.valueOf(channelEvent.getRoomId()));
        assertFalse(channelEvent.getNotify());
        assertEquals(color, channelEvent.getColor());
    }

    @Override
    public DatabaseEntity getDistributionEntity() {
        final MockHipChatEntity mockHipChatEntity = new MockHipChatEntity();
        final HipChatDistributionConfigEntity hipChatDistributionConfigEntity = mockHipChatEntity.createEntity();
        return hipChatDistributionRepositoryAccessor.saveEntity(hipChatDistributionConfigEntity);
    }

    @Override
    public HipChatEventProducer createChannelEventProducer() {
        return new HipChatEventProducer();
    }

    @Override
    public void cleanGlobalRepository() {
        hipChatRepository.deleteAll();
    }

    @Override
    public void cleanDistributionRepositories() {
        hipChatDistributionRepositoryAccessor.deleteAll();
    }

    @Override
    public void saveGlobalConfiguration() {
        final HipChatGlobalConfigEntity globalEntity = new HipChatGlobalConfigEntity(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY), "");
        hipChatRepository.saveEntity(globalEntity);
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return hipChatDescriptor;
    }

    @Override
    public MockRestModelUtil<HipChatDistributionConfig> getMockRestModelUtil() {
        final MockHipChatRestModel restModel = new MockHipChatRestModel();
        restModel.setRoomId(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_ROOM_ID));
        restModel.setNotify(false);
        restModel.setColor("random");
        return restModel;
    }

}
