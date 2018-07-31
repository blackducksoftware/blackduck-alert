package com.blackducksoftware.integration.alert.channel.email.descriptor;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.blackducksoftware.integration.alert.channel.email.EmailGroupChannel;
import com.blackducksoftware.integration.alert.common.descriptor.config.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.common.descriptor.config.DescriptorConfig;
import com.blackducksoftware.integration.alert.common.descriptor.config.UIComponent;
import com.blackducksoftware.integration.alert.database.RepositoryAccessor;
import com.blackducksoftware.integration.alert.database.channel.email.EmailGlobalConfigEntity;
import com.blackducksoftware.integration.alert.database.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.web.channel.model.EmailGlobalConfig;
import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.exception.IntegrationException;

public class EmailGlobalDescriptorConfig extends DescriptorConfig {
    public static final String NOT_AN_INTEGER = "Not an Integer.";

    private final EmailGroupChannel emailGroupChannel;

    public EmailGlobalDescriptorConfig(final DatabaseContentConverter databaseContentConverter, final RepositoryAccessor repositoryAccessor, final UIComponent uiComponent, final EmailGlobalStartupComponent startupComponent,
            final EmailGroupChannel emailGroupChannel) {
        super(databaseContentConverter, repositoryAccessor);
        this.emailGroupChannel = emailGroupChannel;
        setStartupComponent(startupComponent);
    }

    @Override
    public void validateConfig(final Config restModel, final Map<String, String> fieldErrors) {
        final EmailGlobalConfig emailRestModel = (EmailGlobalConfig) restModel;

        if (StringUtils.isNotBlank(emailRestModel.getMailSmtpPort()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpPort())) {
            fieldErrors.put("mailSmtpPort", NOT_AN_INTEGER);
        }
        if (StringUtils.isNotBlank(emailRestModel.getMailSmtpConnectionTimeout()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpConnectionTimeout())) {
            fieldErrors.put("mailSmtpConnectionTimeout", NOT_AN_INTEGER);
        }
        if (StringUtils.isNotBlank(emailRestModel.getMailSmtpTimeout()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpTimeout())) {
            fieldErrors.put("mailSmtpTimeout", NOT_AN_INTEGER);
        }
    }

    @Override
    public void testConfig(final DatabaseEntity entity) throws IntegrationException {
        final EmailGlobalConfigEntity emailEntity = (EmailGlobalConfigEntity) entity;
        emailGroupChannel.testGlobalConfig(emailEntity);
    }

    @Override
    public UIComponent getUiComponent() {
        return new UIComponent("Email", "envelope", "EmailConfiguration");
    }

}
