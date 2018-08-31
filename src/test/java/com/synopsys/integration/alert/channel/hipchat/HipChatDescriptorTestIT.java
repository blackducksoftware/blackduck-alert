package com.synopsys.integration.alert.channel.hipchat;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.DescriptorTestConfigTest;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatEntity;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalRepository;
import com.synopsys.integration.alert.web.channel.model.HipChatDistributionConfig;

public class HipChatDescriptorTestIT extends DescriptorTestConfigTest<HipChatDistributionConfig, HipChatDistributionConfigEntity, HipChatGlobalConfigEntity> {

    @Autowired
    private HipChatGlobalRepository hipChatRepository;

    @Autowired
    private HipChatDistributionRepository distributionRepository;

    @Autowired
    private HipChatDescriptor hipChatDescriptor;

    @Override
    public void cleanGlobalRepository() {
        hipChatRepository.deleteAll();
    }

    @Override
    public void saveGlobalConfiguration() {
        final HipChatGlobalConfigEntity globalEntity = new HipChatGlobalConfigEntity(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY), "");
        hipChatRepository.save(globalEntity);
    }

    @Override
    public void cleanDistributionRepository() {
        distributionRepository.deleteAll();
    }

    @Override
    public MockHipChatEntity getMockEntityUtil() {
        final MockHipChatEntity entity = new MockHipChatEntity();
        entity.setRoomId(Integer.parseInt(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_ROOM_ID)));
        entity.setNotify(false);
        entity.setColor("random");
        return entity;
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return hipChatDescriptor;
    }

}
