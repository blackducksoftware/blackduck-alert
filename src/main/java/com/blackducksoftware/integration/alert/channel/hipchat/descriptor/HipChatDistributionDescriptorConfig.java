package com.blackducksoftware.integration.alert.channel.hipchat.descriptor;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.alert.channel.DistributionChannelManager;
import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.descriptor.config.UIComponent;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatDistributionRepositoryAccessor;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.channel.model.HipChatDistributionConfig;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.exception.IntegrationException;

public class HipChatDistributionDescriptorConfig extends DescriptorConfig {
    private final DistributionChannelManager distributionChannelManager;
    private final HipChatChannel hipChatChannel;

    public HipChatDistributionDescriptorConfig(final HipChatDistributionContentConverter databaseContentConverter, final HipChatDistributionRepositoryAccessor repositoryAccessor,
            final DistributionChannelManager distributionChannelManager, final HipChatChannel hipChatChannel) {
        super(databaseContentConverter, repositoryAccessor);
        this.distributionChannelManager = distributionChannelManager;
        this.hipChatChannel = hipChatChannel;
    }

    @Override
    public UIComponent getUiComponent() {
        return new UIComponent("HipChat", "comments", "HipChatJobConfiguration");
    }

    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {
        final HipChatDistributionConfig hipChatRestModel = (HipChatDistributionConfig) restModel;
        if (StringUtils.isBlank(hipChatRestModel.getRoomId())) {
            fieldErrors.put("roomId", "A Room Id is required.");
        } else if (!StringUtils.isNumeric(hipChatRestModel.getRoomId())) {
            fieldErrors.put("roomId", "Room Id must be an integer value");
        }
    }

    @Override
    public void testConfig(final DatabaseEntity entity) throws IntegrationException {
        final HipChatDistributionConfigEntity hipChatEntity = (HipChatDistributionConfigEntity) entity;
        final ChannelEvent event = distributionChannelManager.createChannelEvent(HipChatChannel.COMPONENT_NAME);
        hipChatChannel.sendAuditedMessage(event, hipChatEntity);
    }

}
