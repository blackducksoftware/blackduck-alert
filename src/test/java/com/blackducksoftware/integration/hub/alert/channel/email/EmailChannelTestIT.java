package com.blackducksoftware.integration.hub.alert.channel.email;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.OutputLogger;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.TestPropertyKey;
import com.blackducksoftware.integration.hub.alert.audit.repository.AuditEntryRepository;
import com.blackducksoftware.integration.hub.alert.channel.ChannelTest;
import com.blackducksoftware.integration.hub.alert.channel.email.mock.MockEmailEntity;
import com.blackducksoftware.integration.hub.alert.channel.email.repository.global.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.global.GlobalHubConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.global.GlobalHubRepository;
import com.blackducksoftware.integration.hub.alert.digest.model.DigestModel;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.ChannelEvent;
import com.blackducksoftware.integration.test.annotation.ExternalConnectionTest;

public class EmailChannelTestIT extends ChannelTest {

    @Test
    @Category(ExternalConnectionTest.class)
    public void sendEmailTest() throws Exception {
        final AuditEntryRepository auditEntryRepository = Mockito.mock(AuditEntryRepository.class);
        final GlobalHubRepository globalRepository = Mockito.mock(GlobalHubRepository.class);
        final GlobalHubConfigEntity globalConfig = new GlobalHubConfigEntity(300, properties.getProperty(TestPropertyKey.TEST_HUB_API_KEY));
        Mockito.when(globalRepository.findAll()).thenReturn(Arrays.asList(globalConfig));

        final TestGlobalProperties globalProperties = new TestGlobalProperties(globalRepository);
        globalProperties.setHubUrl(properties.getProperty(TestPropertyKey.TEST_HUB_SERVER_URL));

        final String trustCert = properties.getProperty(TestPropertyKey.TEST_TRUST_HTTPS_CERT);
        if (trustCert != null) {
            globalProperties.setHubTrustCertificate(Boolean.valueOf(trustCert));
        }

        EmailGroupChannel emailChannel = new EmailGroupChannel(gson, globalProperties, auditEntryRepository, null, null, null, contentConverter);
        final Collection<ProjectData> projectData = createProjectData("Manual test project");
        final DigestModel digestModel = new DigestModel(projectData);
        final ChannelEvent event = new ChannelEvent(EmailGroupChannel.COMPONENT_NAME, contentConverter.convertToString(digestModel), 1L);

        final String smtpHost = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        final String smtpFrom = properties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        final GlobalEmailConfigEntity emailGlobalConfigEntity = new GlobalEmailConfigEntity(smtpHost, null, null, null, null, null, null, smtpFrom, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        emailChannel = Mockito.spy(emailChannel);
        Mockito.doReturn(emailGlobalConfigEntity).when(emailChannel).getGlobalConfigEntity();

        final MockEmailEntity mockEmailEntity = new MockEmailEntity();
        mockEmailEntity.setGroupName("IntegrationTest");
        emailChannel.sendAuditedMessage(event, mockEmailEntity.createEntity());
    }

    @Test
    public void sendEmailNullGlobalTest() throws Exception {
        final OutputLogger outputLogger = new OutputLogger();

        final EmailGroupChannel emailChannel = new EmailGroupChannel(gson, null, null, null, null, null, contentConverter);
        final DigestModel digestModel = new DigestModel(null);
        final ChannelEvent event = new ChannelEvent(EmailGroupChannel.COMPONENT_NAME, contentConverter.convertToString(digestModel), 1L);
        emailChannel.sendMessage(event, null);
        assertTrue(outputLogger.isLineContainingText("No configuration found with id"));

        outputLogger.close();
    }

}
