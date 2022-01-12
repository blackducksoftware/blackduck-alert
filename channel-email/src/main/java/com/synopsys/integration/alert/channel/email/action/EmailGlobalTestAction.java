/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.action;

import java.util.function.Supplier;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.database.accessor.EmailGlobalConfigAccessor;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessageModel;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessagingService;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.ConfigurationTestResult;
import com.synopsys.integration.alert.common.message.model.MessageResult;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.api.ConfigurationTestHelper;
import com.synopsys.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;
import com.synopsys.integration.alert.service.email.EmailTarget;
import com.synopsys.integration.alert.service.email.JavamailPropertiesFactory;
import com.synopsys.integration.alert.service.email.SmtpConfig;
import com.synopsys.integration.alert.service.email.SmtpConfigBuilder;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalTestAction {
    private static final String TEST_SUBJECT_LINE = "Email Global Configuration Test";
    private static final String TEST_MESSAGE_CONTENT = "This is a test message from Alert to confirm your Global Email Configuration is valid.";

    private final ConfigurationValidationHelper validationHelper;
    private final ConfigurationTestHelper testHelper;
    private final EmailGlobalConfigurationValidator validator;
    private final EmailGlobalConfigAccessor configurationAccessor;

    private final EmailChannelMessagingService emailChannelMessagingService;
    private final JavamailPropertiesFactory javamailPropertiesFactory;

    @Autowired
    public EmailGlobalTestAction(AuthorizationManager authorizationManager, EmailGlobalConfigurationValidator validator,
        EmailChannelMessagingService emailChannelMessagingService, JavamailPropertiesFactory javamailPropertiesFactory, EmailGlobalConfigAccessor configurationAccessor) {
        this.testHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
        this.validator = validator;
        this.emailChannelMessagingService = emailChannelMessagingService;
        this.javamailPropertiesFactory = javamailPropertiesFactory;
        this.configurationAccessor = configurationAccessor;
    }

    public ActionResponse<ValidationResponseModel> testWithPermissionCheck(String testAddress, EmailGlobalConfigModel requestResource) {
        Supplier<ValidationActionResponse> validationSupplier = () -> validationHelper.validate(() -> validator.validate(requestResource));
        return testHelper.test(validationSupplier, () -> testConfigModelContent(testAddress, requestResource));
    }

    public ConfigurationTestResult testConfigModelContent(String testAddress, EmailGlobalConfigModel emailGlobalConfigModel) {
        if (StringUtils.isBlank(testAddress)) {
            return ConfigurationTestResult.failure("Could not determine what email address to send this content to. testAddress was not provided or was blank. Please provide a valid email address to test the configuration.");
        }

        try {
            InternetAddress emailAddress = new InternetAddress(testAddress);
            emailAddress.validate();
        } catch (AddressException ex) {
            return ConfigurationTestResult.failure(String.format("%s is not a valid email address. %s", testAddress, ex.getMessage()));
        }

        EmailChannelMessageModel testMessage = EmailChannelMessageModel.simple(TEST_SUBJECT_LINE, TEST_MESSAGE_CONTENT, "", "");

        SmtpConfigBuilder smtpConfigBuilder = SmtpConfig.builder();
        smtpConfigBuilder.setJavamailProperties(javamailPropertiesFactory.createJavaMailProperties(emailGlobalConfigModel));

        emailGlobalConfigModel.getSmtpFrom().ifPresent(smtpConfigBuilder::setSmtpFrom);
        emailGlobalConfigModel.getSmtpHost().ifPresent(smtpConfigBuilder::setSmtpHost);
        emailGlobalConfigModel.getSmtpPort().ifPresent(smtpConfigBuilder::setSmtpPort);
        emailGlobalConfigModel.getSmtpAuth().ifPresent(smtpConfigBuilder::setSmtpAuth);
        emailGlobalConfigModel.getSmtpUsername().ifPresent(smtpConfigBuilder::setSmtpUsername);
        
        if (BooleanUtils.toBoolean(emailGlobalConfigModel.getIsSmtpPasswordSet()) && emailGlobalConfigModel.getSmtpPassword().isEmpty()) {
            //TODO: This assumes if the password is saved but not provided we only test using the default configuration password.
            //  If the UI supports multiple configurations in the future we should determine which configuration to get the password from.
            configurationAccessor.getConfigurationByName(AlertRestConstants.DEFAULT_CONFIGURATION_NAME)
                .flatMap(EmailGlobalConfigModel::getSmtpPassword)
                .ifPresent(emailGlobalConfigModel::setSmtpPassword);
        }
        emailGlobalConfigModel.getSmtpPassword().ifPresent(smtpConfigBuilder::setSmtpPassword);

        SmtpConfig smtpConfig = smtpConfigBuilder.build();

        try {
            EmailTarget emailTarget = emailChannelMessagingService.createTarget(testMessage, testAddress);
            MessageResult messageResult = emailChannelMessagingService.sendMessage(smtpConfig, emailTarget);
            return ConfigurationTestResult.success(messageResult.getStatusMessage());
        } catch (AlertException ex) {
            return ConfigurationTestResult.failure(ex.getMessage());
        }
    }

}
