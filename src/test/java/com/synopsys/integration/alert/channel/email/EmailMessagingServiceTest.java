package com.synopsys.integration.alert.channel.email;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.TestProperties;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.configuration.FieldAccessor;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.database.api.configuration.ConfigurationFieldModel;

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

        final Message message = new MimeMessage(mockSession);
        Mockito.doNothing().when(mockTransport).sendMessage(Mockito.eq(message), Mockito.any());

        emailMessagingService.sendMessage(emailProperties, mockSession, message);
    }

    private FieldAccessor createEmailGlobalConfigEntity(final TestProperties testProperties) {
        final Map<String, ConfigurationFieldModel> fieldMap = new LinkedHashMap<>();
        for (final EmailPropertyKeys emailKey : EmailPropertyKeys.values()) {
            final String key = emailKey.getPropertyKey();
            fieldMap.put(key, ConfigurationFieldModel.create(key));
        }
        final FieldAccessor fieldAccessor = new FieldAccessor(fieldMap);
        return fieldAccessor;
    }
}
