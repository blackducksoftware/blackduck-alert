/*
 * service-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email;

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
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.service.email.template.FreemarkerTemplatingService;
import com.synopsys.integration.exception.IntegrationException;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

@Component
public class EmailMessagingService {
    public static final String EMAIL_SUBJECT_LINE_TEMPLATE = "subjectLineTemplate";
    private final Logger logger = LoggerFactory.getLogger(EmailMessagingService.class);

    private final FreemarkerTemplatingService freemarkerTemplatingService;

    @Autowired
    public EmailMessagingService(FreemarkerTemplatingService freemarkerTemplatingService) {
        this.freemarkerTemplatingService = freemarkerTemplatingService;
    }

    public void sendEmailMessage(Properties javamailProperties, EmailTarget emailTarget) throws AlertException {
        try {
            String templateName = StringUtils.trimToEmpty(emailTarget.getTemplateName());
            Set<String> emailAddresses = emailTarget.getEmailAddresses()
                                             .stream()
                                             .map(String::trim)
                                             .filter(StringUtils::isNotBlank)
                                             .collect(Collectors.toSet());
            if (emailAddresses.isEmpty() || StringUtils.isBlank(templateName)) {
                // Nothing to send
                logger.debug("No non-blank email addresses were provided");
                return;
            }

            Map<String, Object> model = emailTarget.getModel();
            Session session = Session.getInstance(javamailProperties);
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

            String from = javamailProperties.getProperty(EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey());
            boolean auth = Boolean.parseBoolean(javamailProperties.getProperty(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey()));
            String host = javamailProperties.getProperty(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey());
            int port = Integer.parseInt(javamailProperties.getProperty(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()));
            String username = javamailProperties.getProperty(EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey());

            // Not technically a JavaMail prop
            String password = javamailProperties.getProperty(EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey());

            List<Message> messages = createMessages(emailAddresses, resolvedSubjectLine, session, mimeMultipart, from);
            sendMessages(auth, host, port, username, password, session, messages);
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

    private List<Message> createMessages(Collection<String> emailAddresses, String subjectLine, Session session, MimeMultipart mimeMultipart, String fromString)
        throws AlertException, MessagingException {
        List<InternetAddress> addresses = new ArrayList<>();
        Set<String> invalidAddresses = new HashSet<>();
        for (String emailAddress : emailAddresses) {
            try {
                InternetAddress toAddress = new InternetAddress(emailAddress);
                toAddress.validate();
                addresses.add(toAddress);
            } catch (AddressException e) {
                invalidAddresses.add(emailAddress);
                logger.warn("Could not create the address from {}: {}", emailAddress, e.getMessage());
            }
        }

        if (addresses.isEmpty()) {
            String noValidAddressesErrorMessage = "There were no valid email addresses supplied.";
            if (!invalidAddresses.isEmpty()) {
                String invalidAddressesString = StringUtils.join(invalidAddresses, ", ");
                noValidAddressesErrorMessage = String.format("%s Invalid addresses: %s", noValidAddressesErrorMessage, invalidAddressesString);
            }
            throw new AlertException(noValidAddressesErrorMessage);
        }

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

    public void sendMessages(boolean auth, String host, int port, String username, String password, Session session, List<Message> messages) throws AlertException {
        Set<String> errorMessages = new HashSet<>();
        Set<String> invalidRecipients = new HashSet<>();
        for (Message message : messages) {
            Address[] recipients = null;
            try {
                recipients = message.getAllRecipients();
                if (auth) {
                    sendAuthenticated(host, port, username, password, message, session);
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
            String joinedErrorMessages = StringUtils.join(errorMessages, ", ");
            String errorMessage;
            if (invalidRecipients.isEmpty()) {
                errorMessage = "Errors sending emails. " + joinedErrorMessages;
            } else {
                errorMessage = String.format("Error sending emails to the following recipients: %s. %s.", invalidRecipients, joinedErrorMessages);
            }
            throw new AlertException(errorMessage);
        }
    }

    private void sendAuthenticated(String host, int port, String username, String password, Message message, Session session) throws MessagingException {
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
