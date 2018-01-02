package com.blackducksoftware.integration.hub.alert.channel.email;

import java.util.Arrays;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTest;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalSchedulingRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;

public class EmailChannelTestIT extends ChannelTest {

    @Test
    public void sendEmailTest() throws Exception {
        final GlobalHubRepositoryWrapper globalRepository = Mockito.mock(GlobalHubRepositoryWrapper.class);
        final GlobalHubConfigEntity globalConfig = new GlobalHubConfigEntity(300, properties.getProperty(TestPropertyKey.TEST_USERNAME), properties.getProperty(TestPropertyKey.TEST_PASSWORD));
        Mockito.when(globalRepository.findAll()).thenReturn(Arrays.asList(globalConfig));
        final GlobalSchedulingRepositoryWrapper globalSchedulingRepository = Mockito.mock(GlobalSchedulingRepositoryWrapper.class);

        final TestGlobalProperties globalProperties = new TestGlobalProperties(globalRepository, globalSchedulingRepository);
        globalProperties.setHubUrl(properties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));

        final String trustCert = properties.getProperty(TestPropertyKey.TEST_TRUST_HTTPS_CERT);
        if (trustCert != null) {
            globalProperties.setHubTrustCertificate(Boolean.valueOf(trustCert));
        }

        EmailGroupChannel emailChannel = new EmailGroupChannel(globalProperties, gson, null, null, null);
        final ProjectData projectData = createProjectData("Manual test project");
        final EmailGroupEvent event = new EmailGroupEvent(projectData, null);

        final String smtpHost = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        final String smtpFrom = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        final String emailTemplate = properties.getProperty(TestPropertyKey.TEST_EMAIL_TEMPLATE);
        final String logo = properties.getProperty(TestPropertyKey.TEST_EMAIL_LOGO);
        final String subjectLine = "Test Subject Line";
        final GlobalEmailConfigEntity emailConfigEntity = new GlobalEmailConfigEntity(smtpHost, null, null, null, null, null, smtpFrom, null, null, null, null, null, null, null, emailTemplate, logo, subjectLine);

        emailChannel = Mockito.spy(emailChannel);
        Mockito.doReturn(emailConfigEntity).when(emailChannel).getGlobalConfigEntity();

        emailChannel.sendMessage(Arrays.asList(properties.getProperty(TestPropertyKey.TEST_EMAIL_RECIPIENT)), event);
    }

}
