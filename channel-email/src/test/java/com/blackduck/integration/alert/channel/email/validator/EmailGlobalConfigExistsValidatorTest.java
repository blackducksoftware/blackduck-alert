package com.blackduck.integration.alert.channel.email.validator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.api.descriptor.EmailChannelKey;
import com.blackduck.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;

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
