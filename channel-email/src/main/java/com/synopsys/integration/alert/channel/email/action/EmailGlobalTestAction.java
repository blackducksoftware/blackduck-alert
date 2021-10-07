/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.action;

import java.util.function.Supplier;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessageModel;
import com.synopsys.integration.alert.channel.email.distribution.EmailChannelMessagingService;
import com.synopsys.integration.alert.channel.email.validator.EmailGlobalConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.message.model.MessageResult;
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

    private final EmailChannelMessagingService emailChannelMessagingService;
    private final JavamailPropertiesFactory javamailPropertiesFactory;

    @Autowired
    public EmailGlobalTestAction(AuthorizationManager authorizationManager, EmailGlobalConfigurationValidator validator,
        EmailChannelMessagingService emailChannelMessagingService, JavamailPropertiesFactory javamailPropertiesFactory) {
        this.testHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, ChannelKeys.EMAIL);
        this.validator = validator;
        this.emailChannelMessagingService = emailChannelMessagingService;
        this.javamailPropertiesFactory = javamailPropertiesFactory;
    }

    public ActionResponse<ValidationResponseModel> testWithPermissionCheck(String testAddress, EmailGlobalConfigModel requestResource) {
        Supplier<ValidationActionResponse> validationSupplier = () -> validationHelper.validate(() -> validator.validate(requestResource));
        return testHelper.test(validationSupplier, () -> testConfigModelContent(testAddress, requestResource));
    }

    public MessageResult testConfigModelContent(String testAddress, EmailGlobalConfigModel emailGlobalConfigModel) throws AlertException {
        if (StringUtils.isBlank(testAddress)) {
            throw new AlertException("Could not determine what email address to send this content to. testAddress was not provided or was blank. Please provide a valid email address to test the configuration.");
        }

        try {
            InternetAddress emailAddress = new InternetAddress(testAddress);
            emailAddress.validate();
        } catch (AddressException ex) {
            throw new AlertException(String.format("%s is not a valid email address. %s", testAddress, ex.getMessage()));
        }

        EmailChannelMessageModel testMessage = EmailChannelMessageModel.simple(TEST_SUBJECT_LINE, TEST_MESSAGE_CONTENT, "", "");

        SmtpConfigBuilder smtpConfigBuilder = SmtpConfig.builder();
        smtpConfigBuilder.setJavamailProperties(javamailPropertiesFactory.createJavaMailProperties(emailGlobalConfigModel));

        emailGlobalConfigModel.getFrom().ifPresent(smtpConfigBuilder::setSmtpFrom);
        emailGlobalConfigModel.getHost().ifPresent(smtpConfigBuilder::setSmtpHost);
        emailGlobalConfigModel.getPort().ifPresent(smtpConfigBuilder::setSmtpPort);
        emailGlobalConfigModel.getAuth().ifPresent(smtpConfigBuilder::setSmtpAuth);
        emailGlobalConfigModel.getUsername().ifPresent(smtpConfigBuilder::setSmtpUsername);
        emailGlobalConfigModel.getPassword().ifPresent(smtpConfigBuilder::setSmtpPassword);

        SmtpConfig smtpConfig = smtpConfigBuilder.build();

        EmailTarget emailTarget = emailChannelMessagingService.createTarget(testMessage, testAddress);

        return emailChannelMessagingService.sendMessage(smtpConfig, emailTarget);
    }

}
