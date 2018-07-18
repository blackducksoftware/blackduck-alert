package com.blackducksoftware.integration.alert.channel.hipchat;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.alert.TestPropertyKey;
import com.blackducksoftware.integration.alert.channel.ChannelManagerTest;
import com.blackducksoftware.integration.alert.channel.hipchat.mock.MockHipChatRestModel;
import com.blackducksoftware.integration.alert.common.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatGlobalRepository;
import com.blackducksoftware.integration.alert.web.channel.model.HipChatDistributionRestModel;

public class HipChatManagerTestIT extends ChannelManagerTest<HipChatDistributionRestModel, HipChatDistributionConfigEntity, HipChatGlobalConfigEntity> {

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
