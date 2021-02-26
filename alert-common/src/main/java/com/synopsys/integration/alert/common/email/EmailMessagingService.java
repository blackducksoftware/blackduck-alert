/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.email;

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

import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.exception.IntegrationException;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class EmailMessagingService {
    public static final String EMAIL_SUBJECT_LINE_TEMPLATE = "subjectLineTemplate";
    private final Logger logger = LoggerFactory.getLogger(EmailMessagingService.class);

    private final EmailProperties emailProperties;
    private final FreemarkerTemplatingService freemarkerTemplatingService;

    public EmailMessagingService(EmailProperties emailProperties, FreemarkerTemplatingService freemarkerTemplatingService) {
        this.emailProperties = emailProperties;
        this.freemarkerTemplatingService = freemarkerTemplatingService;
    }

    public void sendEmailMessage(EmailTarget emailTarget) throws AlertException {
        try {
            String templateName = StringUtils.trimToEmpty(emailTarget.getTemplateName());
            Set<String> emailAddresses = emailTarget.getEmailAddresses()
                                             .stream()
                                             .map(String::trim)
                                             .filter(StringUtils::isNotBlank)
                                             .collect(Collectors.toSet());
            if (emailAddresses.isEmpty() || StringUtils.isBlank(templateName)) {
                // Nothing to send
                return;
            }

            Map<String, Object> model = emailTarget.getModel();
            Session session = createMailSession(emailProperties);
            TemplateLoader templateLoader = freemarkerTemplatingService.createClassTemplateLoader("/templates/email");
            Configuration templateDirectory = freemarkerTemplatingService.createFreemarkerConfig(templateLoader);
            Template emailTemplate = templateDirectory.getTemplate(templateName);
            String html = freemarkerTemplatingService.resolveTemplate(model, emailTemplate);

            MimeMultipartBuilder mimeMultipartBuilder = new MimeMultipartBuilder();
            mimeMultipartBuilder.addHtmlContent(html);
            mimeMultipartBuilder.addTextContent(Jsoup.parse(html).text());
            mimeMultipartBuilder.addEmbeddedImages(emailTarget.getContentIdsToFilePaths());

            List<String> attachmentFilePaths = emailTarget.getAttachmentFilePaths();
            if (!attachmentFilePaths.isEmpty()) {
                mimeMultipartBuilder.addAttachments(attachmentFilePaths);
            }
            MimeMultipart mimeMultipart = mimeMultipartBuilder.build();

            String subjectLine = (String) model.get(EmailPropertyKeys.TEMPLATE_KEY_SUBJECT_LINE.getPropertyKey());
            if (StringUtils.isBlank(subjectLine)) {
                subjectLine = "Default Subject Line - please define one";
            }
            Template subjectLineTemplate = new Template(EMAIL_SUBJECT_LINE_TEMPLATE, subjectLine, templateDirectory);
            String resolvedSubjectLine = freemarkerTemplatingService.resolveTemplate(model, subjectLineTemplate);
            List<Message> messages = createMessages(emailAddresses, resolvedSubjectLine, session, mimeMultipart, emailProperties);
            sendMessages(emailProperties, session, messages);
        } catch (MessagingException | IOException | IntegrationException ex) {
            String errorMessage = "Could not send the email. " + ex.getMessage();
            throw new AlertException(errorMessage, ex);
        }
    }

    public void addTemplateImage(Map<String, Object> model, Map<String, String> contentIdsToFilePaths, String key, String value) {
        String cid = generateContentId(key);
        model.put(cleanForFreemarker(key), cid);
        contentIdsToFilePaths.put("<" + cid + ">", value);
    }

    private Session createMailSession(EmailProperties emailProperties) {
        Map<String, String> sessionProps = emailProperties.getJavamailConfigProperties();
        Properties props = new Properties();
        props.putAll(sessionProps);

        return Session.getInstance(props);
    }

    private List<Message> createMessages(Collection<String> emailAddresses, String subjectLine, Session session, MimeMultipart mimeMultipart, EmailProperties emailProperties)
        throws AlertException, MessagingException {
        List<InternetAddress> addresses = new ArrayList<>();
        for (String emailAddress : emailAddresses) {
            try {
                InternetAddress toAddress = new InternetAddress(emailAddress);
                toAddress.validate();
                addresses.add(toAddress);
            } catch (AddressException e) {
                logger.warn("Could not create the address from {}: {}", emailAddress, e.getMessage());
            }
        }

        if (addresses.isEmpty()) {
            throw new AlertException("There were no valid email addresses supplied.");
        }

        String fromString = emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_FROM_KEY);
        InternetAddress fromAddress;
        if (StringUtils.isBlank(fromString)) {
            logger.warn("No 'from' address specified");
            throw new AlertException(String.format("Required field '%s' was blank", EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey()));
        } else {
            fromAddress = new InternetAddress(fromString);
            try {
                fromAddress.validate();
            } catch (AddressException e) {
                logger.warn("Invalid 'from' address specified: {}", fromString);
                throw new AlertException(String.format("'%s' is not a valid email address: %s", fromString, e.getMessage()));
            }
        }
        List<Message> messages = new ArrayList<>(addresses.size());
        for (InternetAddress address : addresses) {
            Message message = new MimeMessage(session);
            message.setContent(mimeMultipart);
            message.setFrom(fromAddress);
            message.setRecipients(Message.RecipientType.TO, new Address[] { address });
            message.setSubject(subjectLine);
            messages.add(message);
        }
        return messages;
    }

    public void sendMessages(EmailProperties emailProperties, Session session, List<Message> messages) throws AlertException {
        Set<String> errorMessages = new HashSet<>();
        Set<String> invalidRecipients = new HashSet<>();
        for (Message message : messages) {
            Address[] recipients = null;
            try {
                recipients = message.getAllRecipients();
                if (Boolean.valueOf(emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_AUTH_KEY))) {
                    sendAuthenticated(emailProperties, message, session);
                } else {
                    Transport.send(message);
                }
            } catch (MessagingException e) {
                if (recipients != null) {
                    Stream.of(recipients).map(Address::toString).forEach(invalidRecipients::add);
                }
                errorMessages.add(e.getMessage());
                logger.error("Could not send this email to the following recipients: {}. Reason: {}", recipients, e.getMessage());
            }
        }
        if (!errorMessages.isEmpty()) {
            String errorMessage;
            if (invalidRecipients.isEmpty()) {
                errorMessage = "Errors sending emails. " + StringUtils.join(errorMessages, ", ");
            } else {
                errorMessage = "Error sending emails to the following recipients: " + invalidRecipients;
            }
            throw new AlertException(errorMessage);
        }
    }

    private void sendAuthenticated(EmailProperties emailProperties, Message message, Session session) throws MessagingException {
        String host = emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_HOST_KEY);
        int port = NumberUtils.toInt(emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_PORT_KEY));
        String username = emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_USER_KEY);
        String password = emailProperties.getJavamailOption(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY);

        Transport transport = session.getTransport("smtp");
        try {
            transport.connect(host, port, username, password);
            transport.sendMessage(message, message.getAllRecipients());
        } finally {
            transport.close();
        }
    }

    private String generateContentId(String value) {
        return value.replaceAll("[^A-Za-z0-9]", "bd").trim() + "@synopsys.com";
    }

    private String cleanForFreemarker(String s) {
        return s.replace(".", "_");
    }

}
