package com.synopsys.integration.alert.component.settings;

import static com.synopsys.integration.alert.util.FieldModelUtil.addConfigurationFieldToMap;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import org.junit.jupiter.api.Tag;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationModel;
import com.synopsys.integration.alert.database.api.user.UserAccessor;
import com.synopsys.integration.alert.database.api.user.UserModel;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;

public class PasswordResetServiceTest {
    @Test
    public void resetPasswordInvalidUserTest() throws AlertException {
        final String invalidUsername = "invalid";
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        Mockito.when(userAccessor.getUser(Mockito.eq(invalidUsername))).thenReturn(Optional.empty());

        final PasswordResetService passwordResetService = new PasswordResetService(null, userAccessor, null);
        try {
            passwordResetService.resetPassword(invalidUsername);
            fail("Expected exception to be thrown");
        } catch (final AlertDatabaseConstraintException e) {
            assertExceptionMessageContainsText(e, "No user exists");
        }
    }

    @Test
    public void resetPasswordNoUserEmailTest() {
        final String username = "username";
        final UserModel userModel = UserModel.of(username, "", null, Set.of());
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        Mockito.when(userAccessor.getUser(Mockito.eq(username))).thenReturn(Optional.of(userModel));

        final PasswordResetService passwordResetService = new PasswordResetService(null, userAccessor, null);
        try {
            passwordResetService.resetPassword(username);
            fail("Expected exception to be thrown");
        } catch (final AlertException e) {
            assertExceptionMessageContainsText(e, "No email address configured");
        }
    }

    @Test
    public void resetPasswordNoEmailConfigurationTest() throws AlertDatabaseConstraintException {
        final String username = "username";
        final UserModel userModel = UserModel.of(username, "", "noreply@synopsys.com", Set.of());
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        Mockito.when(userAccessor.getUser(Mockito.eq(username))).thenReturn(Optional.of(userModel));

        final BaseConfigurationAccessor baseConfigurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
        Mockito.when(baseConfigurationAccessor.getConfigurationByDescriptorNameAndContext(Mockito.eq(EmailChannel.COMPONENT_NAME), Mockito.eq(ConfigContextEnum.GLOBAL))).thenReturn(List.of());

        final PasswordResetService passwordResetService = new PasswordResetService(null, userAccessor, baseConfigurationAccessor);
        try {
            passwordResetService.resetPassword(username);
            fail("Expected exception to be thrown");
        } catch (final AlertException e) {
            assertExceptionMessageContainsText(e, "No global email configuration found");
        }
    }

    @Test
    @Tag(TestTags.DEFAULT_INTEGRATION)
    public void resetPasswordValidTestIT() throws AlertException {
        final String username = "username";
        final UserModel userModel = UserModel.of(username, "", "noreply@synopsys.com", Set.of());
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        Mockito.when(userAccessor.getUser(Mockito.eq(username))).thenReturn(Optional.of(userModel));
        Mockito.when(userAccessor.changeUserPassword(Mockito.eq(username), Mockito.anyString())).thenReturn(true);

        final TestProperties testProperties = new TestProperties();
        final Map<String, ConfigurationFieldModel> keyToFieldMap = new HashMap<>();
        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST));
        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM));
        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD));
        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        final ConfigurationModel emailConfig = Mockito.mock(ConfigurationModel.class);
        Mockito.when(emailConfig.getCopyOfKeyToFieldMap()).thenReturn(keyToFieldMap);

        final BaseConfigurationAccessor baseConfigurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
        Mockito.when(baseConfigurationAccessor.getConfigurationByDescriptorNameAndContext(Mockito.eq(EmailChannel.COMPONENT_NAME), Mockito.eq(ConfigContextEnum.GLOBAL))).thenReturn(List.of(emailConfig));

        final TestAlertProperties alertProperties = new TestAlertProperties();
        final PasswordResetService passwordResetService = new PasswordResetService(alertProperties, userAccessor, baseConfigurationAccessor);
        passwordResetService.resetPassword(username);
    }

    @Test
    public void resetPasswordInvalidEmailConfigTest() throws AlertException {
        final String username = "username";
        final UserModel userModel = UserModel.of(username, "", "noreply@synopsys.com", Set.of());
        final UserAccessor userAccessor = Mockito.mock(UserAccessor.class);
        Mockito.when(userAccessor.getUser(Mockito.eq(username))).thenReturn(Optional.of(userModel));
        Mockito.when(userAccessor.changeUserPassword(Mockito.eq(username), Mockito.anyString())).thenReturn(true);

        final Map<String, ConfigurationFieldModel> keyToFieldMap = new HashMap<>();

        final String hostKey = TestPropertyKey.TEST_EMAIL_SMTP_HOST.getPropertyKey();
        final ConfigurationFieldModel hostModel = ConfigurationFieldModel.create(hostKey);
        hostModel.setFieldValue("invalid host");
        keyToFieldMap.put(hostModel.getFieldKey(), hostModel);

        final String fromKey = TestPropertyKey.TEST_EMAIL_SMTP_FROM.getPropertyKey();
        final ConfigurationFieldModel fromModel = ConfigurationFieldModel.create(fromKey);
        fromModel.setFieldValue("invalid from");
        keyToFieldMap.put(fromModel.getFieldKey(), fromModel);

        final ConfigurationModel emailConfig = Mockito.mock(ConfigurationModel.class);
        Mockito.when(emailConfig.getCopyOfKeyToFieldMap()).thenReturn(keyToFieldMap);

        final BaseConfigurationAccessor baseConfigurationAccessor = Mockito.mock(BaseConfigurationAccessor.class);
        Mockito.when(baseConfigurationAccessor.getConfigurationByDescriptorNameAndContext(Mockito.eq(EmailChannel.COMPONENT_NAME), Mockito.eq(ConfigContextEnum.GLOBAL))).thenReturn(List.of(emailConfig));

        final AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertTemplatesDir()).thenReturn("invalid dir");

        final PasswordResetService passwordResetService = new PasswordResetService(alertProperties, userAccessor, baseConfigurationAccessor);
        try {
            passwordResetService.resetPassword(username);
            fail("Expected exception to be thrown");
        } catch (final AlertException e) {
            // PASS
        }

        Mockito.when(alertProperties.getAlertTemplatesDir()).thenReturn(null);
        try {
            passwordResetService.resetPassword(username);
            fail("Expected exception to be thrown");
        } catch (final AlertException e) {
            // PASS
        }
    }

    private void assertExceptionMessageContainsText(final AlertException e, final String text) {
        assertTrue(e.getMessage().contains(text), String.format("Exception message did not contain the expected text. Expected [%s], Actual [%s]", text, e.getMessage()));
    }
}
