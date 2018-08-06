package com.blackducksoftware.integration.alert.channel.hipchat;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.alert.TestPropertyKey;
import com.blackducksoftware.integration.alert.channel.DescriptorTestConfigTest;
import com.blackducksoftware.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatEntity;
import com.blackducksoftware.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatGlobalRepository;
import com.blackducksoftware.integration.alert.web.channel.model.HipChatDistributionConfig;

public class HipChatManagerTestIT extends DescriptorTestConfigTest<HipChatDistributionConfig, HipChatDistributionConfigEntity, HipChatGlobalConfigEntity> {

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
