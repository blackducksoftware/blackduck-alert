package com.synopsys.integration.alert.channel.email;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionRepository;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;

public class EmailDistributionTypeConverterTestIT extends AlertIntegrationTest {
    @Autowired
    private CommonDistributionRepository commonDistributionRepository;
    @Autowired
    private EmailGroupDistributionRepository emailGroupDistributionRepository;
    @Autowired
    private CommonTypeConverter commonTypeConverter;
    @Autowired
    private ContentConverter contentConverter;

    @After
    public void cleanUp() {
        commonDistributionRepository.deleteAll();
        emailGroupDistributionRepository.deleteAll();
    }

    @Test
    public void populateConfigFromEntityTest() {
        final EmailDistributionTypeConverter emailDistributionTypeConverter = new EmailDistributionTypeConverter(contentConverter, commonTypeConverter, commonDistributionRepository);

        final EmailGroupDistributionConfigEntity emailGroupDistributionConfigEntity = new EmailGroupDistributionConfigEntity("logo", "subject line", false);
        final EmailGroupDistributionConfigEntity savedEmailEntity = emailGroupDistributionRepository.save(emailGroupDistributionConfigEntity);

        final CommonDistributionConfigEntity commonDistributionConfigEntity = new CommonDistributionConfigEntity(savedEmailEntity.getId(), EmailGroupChannel.COMPONENT_NAME, "nice name", "some_provider", FrequencyType.REAL_TIME,
            Boolean.FALSE, "", FormatType.DEFAULT);
        final CommonDistributionConfigEntity savedCommonEntity = commonDistributionRepository.save(commonDistributionConfigEntity);

        final Config config = emailDistributionTypeConverter.populateConfigFromEntity(savedEmailEntity);
        Assert.assertTrue(EmailDistributionConfig.class.isAssignableFrom(config.getClass()));
        final EmailDistributionConfig emailConfig = (EmailDistributionConfig) config;

        Assert.assertEquals(emailGroupDistributionConfigEntity.getEmailSubjectLine(), emailConfig.getEmailSubjectLine());
        Assert.assertEquals(emailGroupDistributionConfigEntity.getEmailTemplateLogoImage(), emailConfig.getEmailTemplateLogoImage());
        Assert.assertEquals(emailGroupDistributionConfigEntity.getProjectOwnerOnly(), emailConfig.getProjectOwnerOnly());

        Assert.assertEquals(savedCommonEntity.getDistributionConfigId().toString(), emailConfig.getDistributionConfigId());
        Assert.assertEquals(savedCommonEntity.getDistributionType(), emailConfig.getDistributionType());
        Assert.assertEquals(savedCommonEntity.getName(), emailConfig.getName());
        Assert.assertEquals(savedCommonEntity.getProviderName(), emailConfig.getProviderName());
        Assert.assertEquals(savedCommonEntity.getFrequency().toString(), emailConfig.getFrequency());
        Assert.assertEquals(savedCommonEntity.getFilterByProject().toString(), emailConfig.getFilterByProject());
    }
}
