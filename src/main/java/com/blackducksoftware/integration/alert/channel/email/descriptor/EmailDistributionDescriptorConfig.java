package com.blackducksoftware.integration.alert.channel.email.descriptor;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.DistributionChannelManager;
import com.blackducksoftware.integration.alert.channel.email.EmailGroupChannel;
import com.blackducksoftware.integration.alert.channel.event.ChannelEvent;
import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.descriptor.config.UIComponent;
import com.blackducksoftware.integration.alert.database.channel.email.EmailDistributionRepositoryAccessor;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.channel.model.EmailDistributionConfig;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.exception.IntegrationException;

@Component
public class EmailDistributionDescriptorConfig extends DescriptorConfig {
    private final EmailGroupChannel emailGroupChannel;
    private final DistributionChannelManager distributionChannelManager;

    @Autowired
    public EmailDistributionDescriptorConfig(final EmailDistributionContentConverter databaseContentConverter, final EmailDistributionRepositoryAccessor repositoryAccessor, final EmailGroupChannel emailGroupChannel,
            final DistributionChannelManager distributionChannelManager) {
        super(databaseContentConverter, repositoryAccessor);
        this.emailGroupChannel = emailGroupChannel;
        this.distributionChannelManager = distributionChannelManager;
    }

    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {
        final EmailDistributionConfig emailRestModel = (EmailDistributionConfig) restModel;

        if (StringUtils.isBlank(emailRestModel.getGroupName())) {
            fieldErrors.put("groupName", "A group must be specified.");
        }
    }

    @Override
    public void testConfig(final DatabaseEntity entity) throws IntegrationException {
        final EmailGroupDistributionConfigEntity emailEntity = (EmailGroupDistributionConfigEntity) entity;
        final ChannelEvent event = distributionChannelManager.createChannelEvent(EmailGroupChannel.COMPONENT_NAME);
        emailGroupChannel.sendAuditedMessage(event, emailEntity);
    }

    @Override
    public UIComponent getUiComponent() {
        return new UIComponent("Email", "envelope", "GroupEmailJobConfiguration");
    }

}
