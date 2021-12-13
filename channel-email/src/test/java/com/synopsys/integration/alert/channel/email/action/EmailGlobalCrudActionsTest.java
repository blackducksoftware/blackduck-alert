package com.synopsys.integration.alert.channel.email.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.EmailChannelKey;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

public class EmailGlobalCrudActionsTest {

    @Test
    public void verifyCorrectPasswordIsSet() throws AlertConfigurationException {
        EmailGlobalConfigAccessor emailGlobalConfigAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        EmailGlobalCrudActions emailGlobalCrudActions = createEmailCrudActionsWithAccessor(emailGlobalConfigAccessor);

        UUID uuid = UUID.randomUUID();
        EmailGlobalConfigModel emailGlobalConfigModelOriginal = createEmailGlobalConfigModel();
        String testPassword = "testPassword";
        emailGlobalConfigModelOriginal.setSmtpPassword(testPassword);
        emailGlobalCrudActions.update(uuid, emailGlobalConfigModelOriginal);

        ArgumentCaptor<EmailGlobalConfigModel> emailGlobalConfigModelCapture = ArgumentCaptor.forClass(EmailGlobalConfigModel.class);
        Mockito.verify(emailGlobalConfigAccessor).updateConfiguration(Mockito.any(), emailGlobalConfigModelCapture.capture());

        EmailGlobalConfigModel capturedEmailConfigModel = emailGlobalConfigModelCapture.getValue();
        Optional<String> capturedPassword = capturedEmailConfigModel.getSmtpPassword();

        assertTrue(capturedPassword.isPresent());
        assertEquals(testPassword, capturedPassword.get());
    }

    private EmailGlobalConfigModel createEmailGlobalConfigModel() {
        EmailGlobalConfigModel emailGlobalConfigModel = new EmailGlobalConfigModel();
        emailGlobalConfigModel.setSmtpHost("host");
        emailGlobalConfigModel.setSmtpFrom("from");

        return emailGlobalConfigModel;
    }

    private EmailGlobalCrudActions createBasicEmailCrudActions() {
        EmailGlobalConfigAccessor emailGlobalConfigAccessor = Mockito.mock(EmailGlobalConfigAccessor.class);
        return createEmailCrudActionsWithAccessor(emailGlobalConfigAccessor);
    }

    private EmailGlobalCrudActions createEmailCrudActionsWithAccessor(EmailGlobalConfigAccessor emailGlobalConfigAccessor) {
        AuthorizationManager authorizationManager = Mockito.mock(AuthorizationManager.class);
        Mockito.doReturn(true).when(authorizationManager).hasCreatePermission(ConfigContextEnum.GLOBAL, new EmailChannelKey());
        Mockito.doReturn(true).when(authorizationManager).hasWritePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class));
        Mockito.doReturn(true).when(authorizationManager).hasDeletePermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class));
        Mockito.doReturn(true).when(authorizationManager).hasReadPermission(Mockito.any(ConfigContextEnum.class), Mockito.any(DescriptorKey.class));

        EmailGlobalConfigurationValidator emailGlobalConfigurationValidator = new EmailGlobalConfigurationValidator();
        return new EmailGlobalCrudActions(authorizationManager, emailGlobalConfigAccessor, emailGlobalConfigurationValidator);
    }
}
