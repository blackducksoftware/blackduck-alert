package com.synopsys.integration.alert.channel.email.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;

class EmailGlobalConfigExistsValidatorTest {

    @Test
    void configExistsTest() {
        EmailChannelKey emailChannelKey = new EmailChannelKey();
        EmailGlobalConfigAccessor mockEmailGlobalConfigAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        Mockito.when(mockEmailGlobalConfigAccessor.doesConfigurationExist()).thenReturn(true);
        EmailGlobalConfigExistsValidator emailGlobalConfigExistsValidator = new EmailGlobalConfigExistsValidator(emailChannelKey, mockEmailGlobalConfigAccessor);

        assertTrue(emailGlobalConfigExistsValidator.exists());
        assertEquals(emailChannelKey, emailGlobalConfigExistsValidator.getDescriptorKey());
    }
}
