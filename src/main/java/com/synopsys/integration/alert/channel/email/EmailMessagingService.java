/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.channel.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.channel.util.FreemarkerTemplatingService;
import com.synopsys.integration.alert.channel.email.template.EmailTarget;
import com.synopsys.integration.alert.channel.email.template.MimeMultipartBuilder;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class EmailMessagingService {
    public static final String EMAIL_SUBJECT_LINE_TEMPLATE = "subjectLineTemplate";
    private final Logger logger = LoggerFactory.getLogger(EmailMessagingService.class);

    private final EmailProperties emailProperties;
    private final FreemarkerTemplatingService freemarkerTemplatingService;

    public EmailMessagingService(final EmailProperties emailProperties, final FreemarkerTemplatingService freemarkerTemplatingService) {
        this.emailProperties = emailProperties;
        this.freemarkerTemplatingService = freemarkerTemplatingService;
    }

    public void sendEmailMessage(final EmailTarget emailTarget) throws AlertException {
        try {
            final String templateName = StringUtils.trimToEmpty(emailTarget.getTemplateName());
            final Set<String> emailAddresses = emailTarget.getEmailAddresses()
                                                   .stream()
                                                   .map(String::trim)
                                                   .filter(StringUtils::isNotBlank)
                                                   .collect(Collectors.toSet());
            if (emailAddresses.isEmpty() || StringUtils.isBlank(templateName)) {
                // Nothing to send
                return;
            }

            final Map<String, Object> model = emailTarget.getModel();
            final Session session = createMailSession(emailProperties);
            final String emailPath = freemarkerTemplatingService.getTemplatePath("email");
            final Configuration templateDirectory = freemarkerTemplatingService.createFreemarkerConfig(emailPath);
            final Template emailTemplate = templateDirectory.getTemplate(templateName);
            final String html = freemarkerTemplatingService.getResolvedTemplate(model, emailTemplate);

            final MimeMultipartBuilder mimeMultipartBuilder = new MimeMultipartBuilder();
            mimeMultipartBuilder.addHtmlContent(html);
            mimeMultipartBuilder.addTextContent(Jsoup.parse(html).text());
            mimeMultipartBuilder.addEmbeddedImages(emailTarget.getContentIdsToFilePaths());
            final MimeMultipart mimeMultipart = mimeMultipartBuilder.build();

            String subjectLine = (String) model.get(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey());
            if (StringUtils.isBlank(subjectLine)) {
                subjectLine = "Default Subject Line - please define one";
            }
            final Template subjectLineTemplate = new Template(EMAIL_SUBJECT_LINE_TEMPLATE, subjectLine, templateDirectory);
            final String resolvedSubjectLine = freemarkerTemplatingService.getResolvedTemplate(model, subjectLineTemplate);
            final List<Message> messages = createMessages(emailAddresses, resolvedSubjectLine, session, mimeMultipart, emailProperties);
            sendMessages(emailProperties, session, messages);
        } catch (final MessagingException | IOException | TemplateException ex) {
            final String errorMessage = "Could not send the email. " + ex.getMessage();
            logger.error(errorMessage, ex);
            throw new AlertException(errorMessage, ex);
        }
    }

    public void addTemplateImage(final Map<String, Object> model, final Map<String, String> contentIdsToFilePaths, final String key, final String value) {
        final String cid = generateContentId(key);
        model.put(cleanForFreemarker(key), cid);
        contentIdsToFilePaths.put("<" + cid + ">", value);
    }

    private Session createMailSession(final EmailProperties emailProperties) {
        final Map<String, String> sessionProps = emailProperties.getJavamailConfigProperties();
        final Properties props = new Properties();
        props.putAll(sessionProps);

        return Session.getInstance(props);
    }

    private List<Message> createMessages(final Collection<String> emailAddresses, final String subjectLine, final Session session, final MimeMultipart mimeMultipart, final EmailProperties emailProperties)
        throws AlertException, MessagingException {
        final List<InternetAddress> addresses = new ArrayList<>();
        for (final String emailAddress : emailAddresses) {
            try {
                final InternetAddress toAddress = new InternetAddress(emailAddress);
                toAddress.validate();
                addresses.add(toAddress);
            } catch (final AddressException e) {
                logger.warn("Could not create the address from {}: {}", emailAddress, e.getMessage());
            }
        }

        if (addresses.isEmpty()) {
            throw new AlertException("There were no valid email addresses supplied.");
        }

        final String fromString = emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_FROM_KEY);
        final InternetAddress fromAddress;
        if (StringUtils.isBlank(fromString)) {
            logger.warn("No 'from' address specified");
            throw new AlertException(String.format("Required field '%s' was blank", EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey()));
        } else {
            fromAddress = new InternetAddress(fromString);
            try {
                fromAddress.validate();
            } catch (final AddressException e) {
                logger.warn("Invalid 'from' address specified: " + fromString);
                throw new AlertException(String.format("'%s' is not a valid email address: %s", fromString, e.getMessage()));
            }
        }
        final List<Message> messages = new ArrayList<>(addresses.size());
        for (final InternetAddress address : addresses) {
            final Message message = new MimeMessage(session);
            message.setContent(mimeMultipart);
            message.setFrom(fromAddress);
            message.setRecipients(Message.RecipientType.TO, new Address[] { address });
            message.setSubject(subjectLine);
            messages.add(message);
        }
        return messages;
    }

    public void sendMessages(final EmailProperties emailProperties, final Session session, final List<Message> messages) throws AlertException {
        final Set<String> errorMessages = new HashSet<>();
        final Set<String> invalidRecipients = new HashSet<>();
        for (final Message message : messages) {
            Address[] recipients = null;
            try {
                recipients = message.getAllRecipients();
                if (Boolean.valueOf(emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_AUTH_KEY))) {
                    sendAuthenticated(emailProperties, message, session);
                } else {
                    Transport.send(message);
                }
            } catch (final MessagingException e) {
                if (recipients != null) {
                    Stream.of(recipients).map(Address::toString).forEach(invalidRecipients::add);
                }
                errorMessages.add(e.getMessage());
                logger.error("Could not send this email to the following recipients: {}. Reason: {}", recipients, e.getMessage());
            }
        }
        if (!errorMessages.isEmpty()) {
            final String errorMessage;
            if (invalidRecipients.isEmpty()) {
                errorMessage = "Errors sending emails. " + StringUtils.join(errorMessages, ", ");
            } else {
                errorMessage = "Error sending emails to the following recipients: " + invalidRecipients;
            }
            throw new AlertException(errorMessage);
        }
    }

    private void sendAuthenticated(final EmailProperties emailProperties, final Message message, final Session session) throws MessagingException {
        final String host = emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_HOST_KEY);
        final int port = NumberUtils.toInt(emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_PORT_KEY));
        final String username = emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_USER_KEY);
        final String password = emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY);

        final Transport transport = session.getTransport("smtp");
        try {
            transport.connect(host, port, username, password);
            transport.sendMessage(message, message.getAllRecipients());
        } finally {
            transport.close();
        }
    }

    private String generateContentId(final String value) {
        return value.replaceAll("[^A-Za-z0-9]", "bd").trim() + "@synopsys.com";
    }

    private String cleanForFreemarker(final String s) {
        return s.replace(".", "_");
    }

}
