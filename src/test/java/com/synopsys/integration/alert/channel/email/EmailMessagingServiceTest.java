package com.synopsys.integration.alert.channel.email;

import java.io.IOException;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;

import org.junit.Test;
import org.mockito.Mockito;

import com.sun.mail.smtp.SMTPMessage;
import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.TestPropertyKey;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.database.channel.email.EmailGlobalConfigEntity;

public class EmailMessagingServiceTest {

    @Test
    public void sendAuthenticatedMessage() throws IOException, MessagingException {
        final AlertProperties alertProperties = new AlertProperties();

        final TestProperties testProperties = new TestProperties();
        final EmailProperties emailProperties = new EmailProperties(createEmailGlobalConfigEntity(testProperties));

        final EmailMessagingService emailMessagingService = new EmailMessagingService(alertProperties, emailProperties);

        final Session mockSession = Mockito.mock(Session.class);
        final Transport mockTransport = Mockito.mock(Transport.class);

        Mockito.doNothing().when(mockTransport).connect();
        Mockito.doNothing().when(mockTransport).close();
        Mockito.when(mockSession.getTransport(Mockito.anyString())).thenReturn(mockTransport);
        Mockito.when(mockSession.getProperties()).thenReturn(testProperties.getProperties());

        final Message message = new SMTPMessage(mockSession);
        Mockito.doNothing().when(mockTransport).sendMessage(Mockito.eq(message), Mockito.any());

        emailMessagingService.sendMessage(emailProperties, mockSession, message);
    }

    private EmailGlobalConfigEntity createEmailGlobalConfigEntity(final TestProperties testProperties) {
        final EmailGlobalConfigEntity emailGlobalConfigEntity = new EmailGlobalConfigEntity(
            testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST),
            "testUser",
            "testPassword",
            Integer.valueOf(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT)),
            null,
            null,
            null,
            testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM),
            null,
            null,
            null,
            Boolean.valueOf(testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO)),
            Boolean.TRUE,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Boolean.TRUE,
            null
        );
        emailGlobalConfigEntity.setId(100L);

        return emailGlobalConfigEntity;
    }
}
