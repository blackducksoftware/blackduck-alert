package com.synopsys.integration.alert.web.channel.controller;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatEntity;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatGlobalEntity;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatRestModel;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.web.controller.ControllerTest;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;

public class HipChatChannelDistributionControllerTestIT extends ControllerTest {

    @Autowired
    HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor;

    @Autowired
    HipChatGlobalRepositoryAccessor hipChatGlobalRepositoryAccessor;

    @Override
    public HipChatDistributionRepositoryAccessor getRepositoryAccessor() {
        return hipChatDistributionRepositoryAccessor;
    }

    @Override
    public DatabaseEntity getEntity() {
        return new MockHipChatEntity().createEntity();
    }

    @Override
    public CommonDistributionConfig getConfig() {
        final MockHipChatRestModel mockHipChatRestModel = new MockHipChatRestModel();
        mockHipChatRestModel.setRoomId(testProperties.getProperty(TestPropertyKey.TEST_HIPCHAT_ROOM_ID));
        return mockHipChatRestModel.createRestModel();
    }

    @Override
    public String getDescriptorName() {
        return HipChatChannel.COMPONENT_NAME;
    }

    @Override
    public Long saveGlobalConfig() {
        final MockHipChatGlobalEntity mockHipChatGlobalEntity = new MockHipChatGlobalEntity();
        mockHipChatGlobalEntity.setApiKey(testProperties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY));
        mockHipChatGlobalEntity.setHostServer("");
        final DatabaseEntity savedEntity = hipChatGlobalRepositoryAccessor.saveEntity(mockHipChatGlobalEntity.createGlobalEntity());
        return savedEntity.getId();
    }

    @Override
    public void deleteGlobalConfig(final long id) {
        hipChatGlobalRepositoryAccessor.deleteEntity(id);
    }

}
