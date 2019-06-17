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

import com.synopsys.integration.alert.channel.util.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.FieldAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestProperties;

public class EmailMessagingServiceTest {

    @Test
    public void sendAuthenticatedMessage() throws MessagingException, AlertException {
        final TestProperties testProperties = new TestProperties();
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final EmailProperties emailProperties = new EmailProperties(createEmailGlobalConfigEntity());

        final FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(testAlertProperties);
        final EmailMessagingService emailMessagingService = new EmailMessagingService(emailProperties, freemarkerTemplatingService);

        final Session mockSession = Mockito.mock(Session.class);
        final Transport mockTransport = Mockito.mock(Transport.class);

        Mockito.doNothing().when(mockTransport).connect();
        Mockito.doNothing().when(mockTransport).close();
        Mockito.when(mockSession.getTransport(Mockito.anyString())).thenReturn(mockTransport);
        Mockito.when(mockSession.getProperties()).thenReturn(testProperties.getProperties());

        final Message message = new MimeMessage(mockSession);
        Mockito.doNothing().when(mockTransport).sendMessage(Mockito.eq(message), Mockito.any());

        emailMessagingService.sendMessages(emailProperties, mockSession, List.of(message));
    }

    private FieldAccessor createEmailGlobalConfigEntity() {
        final ConfigurationFieldModel fieldModel = ConfigurationFieldModel.create(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey());
        fieldModel.setFieldValue("true");
        final Map<String, ConfigurationFieldModel> fieldMap = Map.of(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), fieldModel);

        final FieldAccessor fieldAccessor = new FieldAccessor(fieldMap);
        return fieldAccessor;
    }

}
