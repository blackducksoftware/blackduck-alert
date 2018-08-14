/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

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

import com.synopsys.integration.alert.channel.email.template.EmailTarget;
import com.synopsys.integration.alert.channel.email.template.MimeMultipartBuilder;
import com.synopsys.integration.alert.channel.ChannelFreemarkerTemplatingService;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;

import freemarker.template.TemplateException;

public class EmailMessagingService {
    private final Logger logger = LoggerFactory.getLogger(EmailMessagingService.class);

    private final AlertProperties alertProperties;
    private final EmailProperties emailProperties;
    private final ChannelFreemarkerTemplatingService freemarkerTemplatingService;

    public EmailMessagingService(final AlertProperties alertProperties, final EmailProperties emailProperties) throws IOException {
        this.alertProperties = alertProperties;
        this.emailProperties = emailProperties;
        final String templatesDirectory = alertProperties.getAlertTemplatesDir();
        final String templateDirectoryPath;
        if (StringUtils.isNotBlank(templatesDirectory)) {
            templateDirectoryPath = templatesDirectory + "/email";
        } else {
            templateDirectoryPath = System.getProperties().getProperty("user.dir") + "/src/main/resources/email/templates";
        }
        this.freemarkerTemplatingService = new ChannelFreemarkerTemplatingService(templateDirectoryPath);
    }

    public void sendEmailMessage(final EmailTarget emailTarget) throws AlertException {
        try {
            final String emailAddress = StringUtils.trimToEmpty(emailTarget.getEmailAddress());
            final String templateName = StringUtils.trimToEmpty(emailTarget.getTemplateName());
            final Map<String, Object> model = emailTarget.getModel();
            if (StringUtils.isBlank(emailAddress) || StringUtils.isBlank(templateName)) {
                // we've got nothing to do...might as well get out of here...
                return;
            }

            final Session session = createMailSession(emailProperties);
            final Map<String, String> contentIdsToFilePaths = new HashMap<>();
            final String imagesDirectory = alertProperties.getAlertImagesDir();
            final String imageDirectoryPath;
            if (StringUtils.isNotBlank(imagesDirectory)) {
                imageDirectoryPath = imagesDirectory + "/Ducky-80.png";
            } else {
                imageDirectoryPath = System.getProperties().getProperty("user.dir") + "/src/main/resources/email/images/Ducky-80.png";
            }
            addTemplateImage(model, contentIdsToFilePaths, EmailPropertyKeys.EMAIL_LOGO_IMAGE.getPropertyKey(), imageDirectoryPath);
            final String html = freemarkerTemplatingService.getResolvedTemplate(model, templateName);

            final MimeMultipartBuilder mimeMultipartBuilder = new MimeMultipartBuilder();
            mimeMultipartBuilder.addHtmlContent(html);
            mimeMultipartBuilder.addTextContent(Jsoup.parse(html).text());
            mimeMultipartBuilder.addEmbeddedImages(contentIdsToFilePaths);
            final MimeMultipart mimeMultipart = mimeMultipartBuilder.build();

            final String resolvedSubjectLine = freemarkerTemplatingService.getResolvedSubjectLine(model);
            final Message message = createMessage(emailAddress, resolvedSubjectLine, session, mimeMultipart, emailProperties);
            sendMessage(emailProperties, session, message);
        } catch (final MessagingException | IOException | TemplateException ex) {
            throw new AlertException(ex);
        }
    }

    private void addTemplateImage(final Map<String, Object> model, final Map<String, String> contentIdsToFilePaths, final String key, final String value) {
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

    private Message createMessage(final String emailAddress, final String subjectLine, final Session session, final MimeMultipart mimeMultipart, final EmailProperties emailProperties) throws AlertException, MessagingException {
        final List<InternetAddress> addresses = new ArrayList<>();
        try {
            addresses.add(new InternetAddress(emailAddress));
        } catch (final AddressException e) {
            logger.warn(String.format("Could not create the address from %s: %s", emailAddress, e.getMessage()));
        }

        if (addresses.isEmpty()) {
            throw new AlertException("There were no valid email addresses supplied.");
        }

        final Message message = new MimeMessage(session);
        message.setContent(mimeMultipart);

        message.setFrom(new InternetAddress(emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey())));
        message.setRecipients(Message.RecipientType.TO, addresses.toArray(new Address[addresses.size()]));
        message.setSubject(subjectLine);

        return message;
    }

    public void sendMessage(final EmailProperties emailProperties, final Session session, final Message message) throws MessagingException {
        if (Boolean.valueOf(emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey()))) {
            sendAuthenticated(emailProperties, message, session);
        } else {
            Transport.send(message);
        }
    }

    private void sendAuthenticated(final EmailProperties emailProperties, final Message message, final Session session) throws MessagingException {
        final String host = emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey());
        final int port = NumberUtils.toInt(emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()));
        final String username = emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey());
        final String password = emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey());

        final Transport transport = session.getTransport("smtp");
        try {
            transport.connect(host, port, username, password);
            transport.sendMessage(message, message.getAllRecipients());
        } finally {
            transport.close();
        }
    }

    private String generateContentId(final String value) {
        final String cid = value.replaceAll("[^A-Za-z0-9]", "bd").trim() + "@blackducksoftware.com";
        return cid;
    }

    private String cleanForFreemarker(final String s) {
        return s.replace(".", "_");
    }

}
