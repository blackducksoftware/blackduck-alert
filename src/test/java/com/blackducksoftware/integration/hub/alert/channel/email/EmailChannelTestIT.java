package com.blackducksoftware.integration.hub.alert.channel.email;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTest;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepositoryWrapper;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.test.annotation.ExternalConnectionTest;

public class EmailChannelTestIT extends ChannelTest {

    @Test
    @Category(ExternalConnectionTest.class)
    public void sendEmailTest() throws Exception {
        final AuditEntryRepositoryWrapper auditEntryRepository = Mockito.mock(AuditEntryRepositoryWrapper.class);
        final GlobalHubRepositoryWrapper globalRepository = Mockito.mock(GlobalHubRepositoryWrapper.class);
        final GlobalHubConfigEntity globalConfig = new GlobalHubConfigEntity(300, properties.getProperty(TestPropertyKey.TEST_HUB_API_KEY));
        Mockito.when(globalRepository.findAll()).thenReturn(Arrays.asList(globalConfig));

        final TestGlobalProperties globalProperties = new TestGlobalProperties(globalRepository);
        globalProperties.setHubUrl(properties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));

        final String trustCert = properties.getProperty(TestPropertyKey.TEST_TRUST_HTTPS_CERT);
        if (trustCert != null) {
            globalProperties.setHubTrustCertificate(Boolean.valueOf(trustCert));
        }

        final EmailGroupChannel emailChannel = new EmailGroupChannel(globalProperties, gson, auditEntryRepository, null, null, null);
        final ProjectData projectData = createProjectData("Manual test project");
        final EmailGroupEvent event = new EmailGroupEvent(projectData, 1L);

        final String smtpHost = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        final String smtpFrom = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        // TODO fix
        // final GlobalEmailConfigEntity emailGlobalConfigEntity = new GlobalEmailConfigEntity(smtpHost, null, null, null, null, null, smtpFrom, null, null, null, null, null, null, null);
        //
        // emailChannel = Mockito.spy(emailChannel);
        // Mockito.doReturn(emailGlobalConfigEntity).when(emailChannel).getGlobalConfigEntity();
        //
        // final MockEmailEntity mockEmailEntity = new MockEmailEntity();
        // mockEmailEntity.setGroupName("IntegrationTest");
        // emailChannel.sendAuditedMessage(event, mockEmailEntity.createEntity());
    }

    @Test
    public void sendEmailNullGlobalTest() throws Exception {
        final OutputLogger outputLogger = new OutputLogger();

        final EmailGroupChannel emailChannel = new EmailGroupChannel(null, gson, null, null, null, null);
        emailChannel.sendMessage(new EmailGroupEvent(null, 1L), null);
        assertTrue(outputLogger.isLineContainingText("No configuration found with id"));

        outputLogger.close();
    }

}
