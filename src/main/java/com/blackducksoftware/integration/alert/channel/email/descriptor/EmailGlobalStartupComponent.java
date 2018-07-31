package com.blackducksoftware.integration.alert.channel.email.descriptor;

import java.util.Set;

import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.channel.email.EmailGroupChannel;
import com.blackducksoftware.integration.alert.common.descriptor.config.StartupComponent;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.EntityPropertyMapper;
import com.blackducksoftware.integration.alert.web.channel.model.EmailGlobalConfig;
import com.blackducksoftware.integration.alert.workflow.startup.AlertStartupProperty;

@Component
public class EmailGlobalStartupComponent extends StartupComponent {
    private final EntityPropertyMapper entityPropertyMapper;

    public EmailGlobalStartupComponent(final EntityPropertyMapper entityPropertyMapper) {
        super(new EmailGlobalConfig());
        this.entityPropertyMapper = entityPropertyMapper;
    }

    @Override
    public Set<AlertStartupProperty> getGlobalEntityPropertyMapping() {
        return entityPropertyMapper.mapEntityToProperties(EmailGroupChannel.COMPONENT_NAME, EmailGlobalConfigEntity.class);
    }

}
