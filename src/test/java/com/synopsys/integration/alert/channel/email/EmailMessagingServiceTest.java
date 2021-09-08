package com.synopsys.integration.alert.channel.email;

import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.service.email.EmailMessagingService;
import com.synopsys.integration.alert.service.email.JavamailPropertiesFactory;
import com.synopsys.integration.alert.service.email.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.test.common.TestProperties;

public class EmailMessagingServiceTest {
    @Test
    public void sendAuthenticatedMessage() throws MessagingException, AlertException {
        TestProperties testProperties = new TestProperties();

        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        JavamailPropertiesFactory javamailPropertiesFactory = new JavamailPropertiesFactory();
        EmailMessagingService emailMessagingService = new EmailMessagingService(freemarkerTemplatingService, javamailPropertiesFactory);

        Session mockSession = Mockito.mock(Session.class);
        Transport mockTransport = Mockito.mock(Transport.class);

        Mockito.doNothing().when(mockTransport).connect();
        Mockito.doNothing().when(mockTransport).close();
        Mockito.when(mockSession.getTransport(Mockito.anyString())).thenReturn(mockTransport);
        Mockito.when(mockSession.getProperties()).thenReturn(testProperties.getProperties());

        Message message = new MimeMessage(mockSession);
        Mockito.doNothing().when(mockTransport).sendMessage(Mockito.eq(message), Mockito.any());

        emailMessagingService.sendMessages(true, null, -1, null,null, mockSession, List.of(message));
    }

}
