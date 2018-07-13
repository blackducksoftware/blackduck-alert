package com.blackducksoftware.integration.alert.channel.email;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ContentConverter;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGroupDistributionRestModel;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.DatabaseContentConverter;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;

@Component
public class EmailDistributionContentConverter extends DatabaseContentConverter {
    private final ContentConverter contentConverter;

    @Autowired
    public EmailDistributionContentConverter(final ContentConverter contentConverter) {
        this.contentConverter = contentConverter;
    }

    @Override
    public ConfigRestModel getRestModelFromJson(final String json) {
        final Optional<EmailGroupDistributionRestModel> restModel = contentConverter.getContent(json, EmailGroupDistributionRestModel.class);
        if (restModel.isPresent()) {
            return restModel.get();
        }

        return null;
    }

    @Override
    public DatabaseEntity populateDatabaseEntityFromRestModel(final ConfigRestModel restModel) {
        final EmailGroupDistributionRestModel emailRestModel = (EmailGroupDistributionRestModel) restModel;
        return new EmailGroupDistributionConfigEntity(emailRestModel.getGroupName(), emailRestModel.getEmailTemplateLogoImage(), emailRestModel.getEmailSubjectLine());
    }

    @Override
    public ConfigRestModel populateRestModelFromDatabaseEntity(final DatabaseEntity entity) {
        final EmailGroupDistributionConfigEntity emailEntity = (EmailGroupDistributionConfigEntity) entity;
        final EmailGroupDistributionRestModel emailRestModel = new EmailGroupDistributionRestModel();
        emailRestModel.setGroupName(emailEntity.getGroupName());
        emailRestModel.setEmailTemplateLogoImage(emailEntity.getEmailTemplateLogoImage());
        emailRestModel.setEmailSubjectLine(emailEntity.getEmailSubjectLine());
        return emailRestModel;
    }

}
