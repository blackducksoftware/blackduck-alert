package com.blackducksoftware.integration.hub.alert.channel.hipchat;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.controller.distribution.HipChatDistributionRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.mock.MockHipChatRestModel;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.distribution.HipChatDistributionRepository;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.channel.hipchat.repository.global.GlobalHipChatRepository;
import com.blackducksoftware.integration.hub.alert.channel.manager.ChannelManagerTest;
import com.blackducksoftware.integration.hub.alert.descriptor.ChannelDescriptor;

public class HipChatManagerTestIT extends ChannelManagerTest<HipChatDistributionRestModel, HipChatDistributionConfigEntity, GlobalHipChatConfigEntity> {

    @Autowired
    private GlobalHipChatRepository hipChatRepository;

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
        final GlobalHipChatConfigEntity globalEntity = new GlobalHipChatConfigEntity(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY), "");
        hipChatRepository.save(globalEntity);
    }

    @Override
    public void cleanDistributionRepository() {
        distributionRepository.deleteAll();
    }

    @Override
    public MockHipChatRestModel getMockRestModelUtil() {
        final MockHipChatRestModel restModel = new MockHipChatRestModel();
        restModel.setRoomId(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_ROOM_ID));
        restModel.setNotify(false);
        restModel.setColor("random");
        restModel.setId("");
        return restModel;
    }

    @Override
    public ChannelDescriptor getDescriptor() {
        return hipChatDescriptor;
    }

}
