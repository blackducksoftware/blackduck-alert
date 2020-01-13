package com.synopsys.integration.alert.component.settings;

import static com.synopsys.integration.alert.util.FieldModelUtil.addConfigurationFieldToMap;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.Mockito;

import com.synopsys.integration.alert.channel.email.EmailChannelKey;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.enumeration.AuthenticationType;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.UserModel;
import com.synopsys.integration.alert.database.api.DefaultUserAccessor;
import com.synopsys.integration.alert.util.TestAlertProperties;
import com.synopsys.integration.alert.util.TestProperties;
import com.synopsys.integration.alert.util.TestPropertyKey;
import com.synopsys.integration.alert.util.TestTags;

public class PasswordResetServiceTest {
    private static final EmailChannelKey EMAIL_CHANNEL_KEY = new EmailChannelKey();

    @Test
    public void resetPasswordInvalidUserTest() throws AlertException {
        String invalidUsername = "invalid";
        DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        Mockito.when(userAccessor.getUser(Mockito.eq(invalidUsername))).thenReturn(Optional.empty());

        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(null);
        PasswordResetService passwordResetService = new PasswordResetService(null, userAccessor, null, freemarkerTemplatingService, EMAIL_CHANNEL_KEY);
        try {
            passwordResetService.resetPassword(invalidUsername);
            fail("Expected exception to be thrown");
        } catch (AlertDatabaseConstraintException e) {
            assertExceptionMessageContainsText(e, "No user exists");
        }
    }

    @Test
    public void resetPasswordNoUserEmailTest() {
        String username = "username";
        UserModel userModel = UserModel.newUser(username, "", null, AuthenticationType.DATABASE, Set.of(), true);
        DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        Mockito.when(userAccessor.getUser(Mockito.eq(username))).thenReturn(Optional.of(userModel));

        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(null);
        PasswordResetService passwordResetService = new PasswordResetService(null, userAccessor, null, freemarkerTemplatingService, EMAIL_CHANNEL_KEY);
        try {
            passwordResetService.resetPassword(username);
            fail("Expected exception to be thrown");
        } catch (AlertException e) {
            assertExceptionMessageContainsText(e, "No email address configured");
        }
    }

    @Test
    public void resetPasswordNoEmailConfigurationTest() throws AlertDatabaseConstraintException {
        String username = "username";
        UserModel userModel = UserModel.newUser(username, "", "noreply@synopsys.com", AuthenticationType.DATABASE, Set.of(), true);
        DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        Mockito.when(userAccessor.getUser(Mockito.eq(username))).thenReturn(Optional.of(userModel));

        ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(baseConfigurationAccessor.getConfigurationByDescriptorNameAndContext(Mockito.eq(EMAIL_CHANNEL_KEY.getUniversalKey()), Mockito.eq(ConfigContextEnum.GLOBAL))).thenReturn(List.of());

        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(null);
        PasswordResetService passwordResetService = new PasswordResetService(null, userAccessor, baseConfigurationAccessor, freemarkerTemplatingService, EMAIL_CHANNEL_KEY);
        try {
            passwordResetService.resetPassword(username);
            fail("Expected exception to be thrown");
        } catch (AlertException e) {
            assertExceptionMessageContainsText(e, "No global email configuration found");
        }
    }

    @Test
    @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    public void resetPasswordValidTestIT() throws AlertException {
        TestProperties testProperties = new TestProperties();
        Map<String, ConfigurationFieldModel> keyToFieldMap = new HashMap<>();

        String mailSmtpHost = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_HOST);
        String mailSmtpFrom = testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_FROM);
        assumeTrue(StringUtils.isNotBlank(mailSmtpHost) && StringUtils.isNotBlank(mailSmtpFrom), "Minimum email properties not configured");
        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey(), mailSmtpHost);
        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey(), mailSmtpFrom);

        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_USER));
        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PASSWORD));
        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_EHLO_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_EHLO));
        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_AUTH));
        addConfigurationFieldToMap(keyToFieldMap, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey(), testProperties.getProperty(TestPropertyKey.TEST_EMAIL_SMTP_PORT));

        String username = "username";
        UserModel userModel = UserModel.newUser(username, "", "noreply@synopsys.com", AuthenticationType.DATABASE, Set.of(), true);
        DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        Mockito.when(userAccessor.getUser(Mockito.eq(username))).thenReturn(Optional.of(userModel));
        Mockito.when(userAccessor.changeUserPassword(Mockito.eq(username), Mockito.anyString())).thenReturn(true);

        ConfigurationModel emailConfig = Mockito.mock(ConfigurationModel.class);
        Mockito.when(emailConfig.getCopyOfKeyToFieldMap()).thenReturn(keyToFieldMap);

        ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(baseConfigurationAccessor.getConfigurationByDescriptorKeyAndContext(Mockito.eq(EMAIL_CHANNEL_KEY), Mockito.eq(ConfigContextEnum.GLOBAL))).thenReturn(List.of(emailConfig));

        TestAlertProperties alertProperties = new TestAlertProperties();
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(alertProperties);
        PasswordResetService passwordResetService = new PasswordResetService(alertProperties, userAccessor, baseConfigurationAccessor, freemarkerTemplatingService, EMAIL_CHANNEL_KEY);
        passwordResetService.resetPassword(username);
    }

    @Test
    public void resetPasswordInvalidEmailConfigTest() throws AlertException {
        String username = "username";
        UserModel userModel = UserModel.newUser(username, "", "noreply@synopsys.com", AuthenticationType.DATABASE, Set.of(), true);
        DefaultUserAccessor userAccessor = Mockito.mock(DefaultUserAccessor.class);
        Mockito.when(userAccessor.getUser(Mockito.eq(username))).thenReturn(Optional.of(userModel));
        Mockito.when(userAccessor.changeUserPassword(Mockito.eq(username), Mockito.anyString())).thenReturn(true);

        Map<String, ConfigurationFieldModel> keyToFieldMap = new HashMap<>();

        String hostKey = TestPropertyKey.TEST_EMAIL_SMTP_HOST.getPropertyKey();
        ConfigurationFieldModel hostModel = ConfigurationFieldModel.create(hostKey);
        hostModel.setFieldValue("invalid host");
        keyToFieldMap.put(hostModel.getFieldKey(), hostModel);

        String fromKey = TestPropertyKey.TEST_EMAIL_SMTP_FROM.getPropertyKey();
        ConfigurationFieldModel fromModel = ConfigurationFieldModel.create(fromKey);
        fromModel.setFieldValue("invalid from");
        keyToFieldMap.put(fromModel.getFieldKey(), fromModel);

        ConfigurationModel emailConfig = Mockito.mock(ConfigurationModel.class);
        Mockito.when(emailConfig.getCopyOfKeyToFieldMap()).thenReturn(keyToFieldMap);

        ConfigurationAccessor baseConfigurationAccessor = Mockito.mock(ConfigurationAccessor.class);
        Mockito.when(baseConfigurationAccessor.getConfigurationByDescriptorKeyAndContext(Mockito.eq(EMAIL_CHANNEL_KEY), Mockito.eq(ConfigContextEnum.GLOBAL))).thenReturn(List.of(emailConfig));

        AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getAlertTemplatesDir()).thenReturn("invalid dir");

        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(alertProperties);
        PasswordResetService passwordResetService = new PasswordResetService(alertProperties, userAccessor, baseConfigurationAccessor, freemarkerTemplatingService, EMAIL_CHANNEL_KEY);
        try {
            passwordResetService.resetPassword(username);
            fail("Expected exception to be thrown");
        } catch (AlertException e) {
            // PASS
        }

        Mockito.when(alertProperties.getAlertTemplatesDir()).thenReturn(null);
        try {
            passwordResetService.resetPassword(username);
            fail("Expected exception to be thrown");
        } catch (AlertException e) {
            // PASS
        }
    }

    private void assertExceptionMessageContainsText(AlertException e, String text) {
        assertTrue(e.getMessage().contains(text), String.format("Exception message did not contain the expected text. Expected [%s], Actual [%s]", text, e.getMessage()));
    }

}
