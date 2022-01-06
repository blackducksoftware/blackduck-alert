/*
 * service-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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

    public void sendEmailMessage(Properties javamailProperties, String smtpFrom, String smtpHost, int smtpPort, boolean smtpAuth, String smtpUsername, String smtpPassword, EmailTarget emailTarget) throws AlertException {
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

            List<Message> messages = createMessages(emailAddresses, resolvedSubjectLine, session, mimeMultipart, smtpFrom);
            sendMessages(smtpAuth, smtpHost, smtpPort, smtpUsername, smtpPassword, session, messages);
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

        try (Transport transport = getAndConnectTransport(auth, host, port, username, password, session)) {
            for (Message message : messages) {
                Optional<String> errors = sendMessage(transport, message);
                errors.ifPresent(errorMessages::add);
            }
        } catch (MessagingException e) {
            String errorMessage = "Could not setup the email transport: " + e.getMessage();
            logger.error(errorMessage);
            throw new AlertException(errorMessage, e);
        }
        logger.trace("Transport session closed.");
        if (!errorMessages.isEmpty()) {
            String joinedErrorMessages = StringUtils.join(errorMessages, System.lineSeparator());
            logger.error(joinedErrorMessages);
            throw new AlertException(joinedErrorMessages);
        }
    }

    private Transport getAndConnectTransport(boolean auth, String host, int port, String username, String password, Session session) throws MessagingException {
        Transport transport = session.getTransport();
        if (auth) {
            transport.connect(host, port, username, password);
        } else {
            transport.connect();
        }
        logger.trace("Transport session established.");
        return transport;
    }

    private Optional<String> sendMessage(Transport transport, Message message) {
        Address[] recipients = null;
        try {
            recipients = message.getAllRecipients();
            transport.sendMessage(message, recipients);
        } catch (MessagingException e) {
            Set<String> recipientAddresses = Collections.emptySet();
            if (recipients != null) {
                recipientAddresses = Stream.of(recipients).map(Address::toString).collect(Collectors.toSet());
            }
            String error = String.format("Could not send this email to the following recipients: %s. Reason: %s", recipientAddresses, e.getMessage());
            return Optional.of(error);
        }
        return Optional.empty();
    }

    private String generateContentId(String value) {
        return value.replaceAll("[^A-Za-z0-9]", "bd").trim() + "@synopsys.com";
    }

    private String cleanForFreemarker(String s) {
        return s.replace(".", "_");
    }

}
