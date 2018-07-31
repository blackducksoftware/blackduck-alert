package com.blackducksoftware.integration.alert.channel.hipchat.descriptor;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.alert.common.descriptor.config.StartupComponent;
import com.blackducksoftware.integration.alert.database.channel.hipchat.HipChatGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.EntityPropertyMapper;
import com.blackducksoftware.integration.alert.web.channel.model.HipChatGlobalConfig;
import com.blackducksoftware.integration.alert.workflow.startup.AlertStartupProperty;

@Component
public class HipChatStartupComponent extends StartupComponent {
    private final EntityPropertyMapper entityPropertyMapper;

    @Autowired
    public HipChatStartupComponent(final EntityPropertyMapper entityPropertyMapper) {
        super(new HipChatGlobalConfig());
        this.entityPropertyMapper = entityPropertyMapper;
    }

    @Override
    public Set<AlertStartupProperty> getGlobalEntityPropertyMapping() {
        return entityPropertyMapper.mapEntityToProperties(HipChatChannel.COMPONENT_NAME, HipChatGlobalConfigEntity.class);
    }

}
