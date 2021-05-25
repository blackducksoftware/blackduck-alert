package com.synopsys.integration.alert.channel.email;

import java.util.List;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldUtility;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.service.email.EmailMessagingService;
import com.synopsys.integration.alert.service.email.EmailProperties;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.test.common.TestProperties;

public class EmailMessagingServiceTest {
    @Test
    public void sendAuthenticatedMessage() throws MessagingException, AlertException {
        TestProperties testProperties = new TestProperties();
        EmailProperties emailProperties = new EmailProperties(createEmailGlobalConfigEntity());

        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService();
        EmailMessagingService emailMessagingService = new EmailMessagingService(emailProperties, freemarkerTemplatingService);

        Session mockSession = Mockito.mock(Session.class);
        Transport mockTransport = Mockito.mock(Transport.class);

        Mockito.doNothing().when(mockTransport).connect();
        Mockito.doNothing().when(mockTransport).close();
        Mockito.when(mockSession.getTransport(Mockito.anyString())).thenReturn(mockTransport);
        Mockito.when(mockSession.getProperties()).thenReturn(testProperties.getProperties());

        Message message = new MimeMessage(mockSession);
        Mockito.doNothing().when(mockTransport).sendMessage(Mockito.eq(message), Mockito.any());

        emailMessagingService.sendMessages(emailProperties, mockSession, List.of(message));
    }

    private FieldUtility createEmailGlobalConfigEntity() {
        ConfigurationFieldModel fieldModel = ConfigurationFieldModel.create(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey());
        fieldModel.setFieldValue("true");
        Map<String, ConfigurationFieldModel> fieldMap = Map.of(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), fieldModel);

        FieldUtility fieldUtility = new FieldUtility(fieldMap);
        return fieldUtility;
    }

}
