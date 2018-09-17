package com.synopsys.integration.alert.channel.hipchat;

import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.channel.DescriptorTestConfigTest;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.channel.hipchat.mock.MockHipChatEntity;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionRepository;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatDistributionRepository;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.synopsys.integration.alert.database.channel.hipchat.HipChatGlobalRepository;
import com.synopsys.integration.alert.database.channel.slack.SlackDistributionRepository;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.web.channel.model.HipChatDistributionConfig;

public class HipChatDescriptorTestIT extends DescriptorTestConfigTest<HipChatDistributionConfig, HipChatDistributionConfigEntity, HipChatGlobalConfigEntity> {
    private final HipChatGlobalRepository hipChatRepository;
    private final HipChatDescriptor hipChatDescriptor;

    @Autowired
    public HipChatDescriptorTestIT(final EmailGroupDistributionRepository emailGroupDistributionRepository,
        final HipChatDistributionRepository hipChatDistributionRepository,
        final SlackDistributionRepository slackDistributionRepository,
        final BlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor,
        final BlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor,
        final UserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor, final HipChatGlobalRepository hipChatRepository, final HipChatDescriptor hipChatDescriptor) {
        super(emailGroupDistributionRepository, hipChatDistributionRepository, slackDistributionRepository, blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);
        this.hipChatRepository = hipChatRepository;
        this.hipChatDescriptor = hipChatDescriptor;
    }

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
