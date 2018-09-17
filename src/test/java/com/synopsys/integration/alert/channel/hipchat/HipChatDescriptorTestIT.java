package com.synopsys.integration.alert.channel.hipchat;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.DescriptorTestConfigTest;
import com.synopsys.integration.alert.channel.event.ChannelEventFactory;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatEntity;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalRepository;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepositoryAccessor;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.web.channel.model.HipChatDistributionConfig;

public class HipChatDescriptorTestIT extends DescriptorTestConfigTest<HipChatDistributionConfig, HipChatDistributionConfigEntity, HipChatGlobalConfigEntity> {
    @Autowired
    private EmailDistributionRepositoryAccessor emailDistributionRepositoryAccessor;
    @Autowired
    private HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor;
    @Autowired
    private SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor;
    @Autowired
    private BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor;
    @Autowired
    private BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor;
    @Autowired
    private UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor;
    @Autowired
    private HipChatGlobalRepository hipChatRepository;
    @Autowired
    private HipChatDescriptor hipChatDescriptor;

    @Override
    public DatabaseEntity getDistributionEntity() {
        final MockHipChatEntity mockHipChatEntity = new MockHipChatEntity();
        final HipChatDistributionConfigEntity hipChatDistributionConfigEntity = mockHipChatEntity.createEntity();
        return hipChatDistributionRepositoryAccessor.saveEntity(hipChatDistributionConfigEntity);
    }

    @Override
    public ChannelEventFactory createChannelEventFactory() {
        return new ChannelEventFactory(emailDistributionRepositoryAccessor, hipChatDistributionRepositoryAccessor, slackDistributionRepositoryAccessor,
            blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);
    }

    @Override
    public void cleanGlobalRepository() {
        hipChatRepository.deleteAll();
    }

    @Override
    public void cleanDistributionRepositories() {
        emailDistributionRepositoryAccessor.deleteAll();
        hipChatDistributionRepositoryAccessor.deleteAll();
        slackDistributionRepositoryAccessor.deleteAll();
    }

    @Override
    public void saveGlobalConfiguration() {
        final HipChatGlobalConfigEntity globalEntity = new HipChatGlobalConfigEntity(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY), "");
        hipChatRepository.save(globalEntity);
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
