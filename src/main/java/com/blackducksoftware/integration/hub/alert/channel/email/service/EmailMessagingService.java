/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.channel.email.service;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
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
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.alert.AlertConstants;
import com.blackducksoftware.integration.hub.alert.channel.email.model.EmailTarget;
import com.blackducksoftware.integration.hub.alert.channel.email.model.MimeMultipartBuilder;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;

public class EmailMessagingService {
    private final Logger logger = LoggerFactory.getLogger(EmailMessagingService.class);

    private final EmailProperties emailProperties;
    private final Configuration configuration;

    public EmailMessagingService(final EmailProperties emailProperties) throws IOException {
        this.emailProperties = emailProperties;
        configuration = createFreemarkerConfig();
    }

    public Configuration createFreemarkerConfig() throws IOException {
        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
        final File templateDirectory = findTemplateDirectory();
        cfg.setDirectoryForTemplateLoading(templateDirectory);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setLogTemplateExceptions(false);

        return cfg;
    }

    private File findTemplateDirectory() {
        try {
            File templateDir = null;
            final String appHomeDir = System.getProperty(AlertConstants.SYSTEM_PROPERTY_KEY_APP_HOME);
            if (StringUtils.isNotBlank(appHomeDir)) {
                templateDir = new File(appHomeDir, "templates");
            }

            final String templateDirProperty = emailProperties.getEmailTemplateDirectory();
            if (StringUtils.isNotBlank(templateDirProperty)) {
                templateDir = new File(templateDirProperty);
            }

            return templateDir;
        } catch (final Exception e) {
            logger.error("Error finding the template directory", e);
            return null;
        }
    }

    public void sendEmailMessage(final EmailTarget emailTarget) throws MessagingException, TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
        final String emailAddress = StringUtils.trimToEmpty(emailTarget.getEmailAddress());
        final String templateName = StringUtils.trimToEmpty(emailTarget.getTemplateName());
        final Map<String, Object> model = emailTarget.getModel();
        if (StringUtils.isBlank(emailAddress) || StringUtils.isBlank(templateName)) {
            // we've got nothing to do...might as well get out of here...
            return;
        }

        final Session session = createMailSession(emailProperties);
        final Map<String, String> contentIdsToFilePaths = new HashMap<>();
        populateModelWithAdditionalProperties(emailProperties, model, templateName, contentIdsToFilePaths);
        final String html = getResolvedTemplate(model, templateName);

        final MimeMultipartBuilder mimeMultipartBuilder = new MimeMultipartBuilder();
        mimeMultipartBuilder.addHtmlContent(html);
        mimeMultipartBuilder.addTextContent(Jsoup.parse(html).text());
        mimeMultipartBuilder.addEmbeddedImages(contentIdsToFilePaths);
        final MimeMultipart mimeMultipart = mimeMultipartBuilder.build();

        final String resolvedSubjectLine = getResolvedSubjectLine(model);
        final Message message = createMessage(emailAddress, resolvedSubjectLine, session, mimeMultipart, emailProperties);
        sendMessage(emailProperties, session, message);
    }

    private String getResolvedTemplate(final Map<String, Object> model, final String templateName) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
        final StringWriter stringWriter = new StringWriter();
        final Template template = configuration.getTemplate(templateName);
        template.process(model, stringWriter);
        return stringWriter.toString();
    }

    private String getResolvedSubjectLine(final Map<String, Object> model) throws IOException, TemplateException {
        String subjectLine = (String) model.get(EmailProperties.TEMPLATE_KEY_SUBJECT_LINE);
        if (StringUtils.isBlank(subjectLine)) {
            subjectLine = "Default Subject Line - please define one";
        }
        final Template subjectLineTemplate = new Template("subjectLineTemplate", subjectLine, configuration);
        final StringWriter stringWriter = new StringWriter();
        subjectLineTemplate.process(model, stringWriter);
        return stringWriter.toString();
    }

    private void populateModelWithAdditionalProperties(final EmailProperties customerProperties, final Map<String, Object> model, final String templateName, final Map<String, String> contentIdsToFilePaths) {
        for (final Map.Entry<String, String> entry : customerProperties.getSuppliedTemplateVariableProperties().entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            manageKey(model, templateName, contentIdsToFilePaths, key, value);
        }
    }

    private void manageKey(final Map<String, Object> model, final String templateName, final Map<String, String> contentIdsToFilePaths, String key, final String value) {
        boolean shouldAddToModel = false;
        if (key.contains("all.templates.")) {
            shouldAddToModel = true;
            key = key.replace("all.templates.", "");

            // if we've already added a value for this key using the template
            // name, assume it overrides the 'all.templates' value
            if (model.containsKey(cleanForFreemarker(key))) {
                shouldAddToModel = false;
            }
        } else if (key.contains(templateName + ".")) {
            shouldAddToModel = true;
            key = key.replace(templateName + ".", "");
        }

        if (shouldAddToModel) {
            if (key.endsWith(".image")) {
                final String cid = generateContentId(key);
                model.put(cleanForFreemarker(key), cid);
                contentIdsToFilePaths.put("<" + cid + ">", value);
            } else {
                model.put(cleanForFreemarker(key), value);
            }
        }
    }

    private Session createMailSession(final EmailProperties customerProperties) {
        final Map<String, String> sessionProps = customerProperties.getPropertiesForSession();
        final Properties props = new Properties();
        props.putAll(sessionProps);

        return Session.getInstance(props);
    }

    private Message createMessage(final String emailAddress, final String subjectLine, final Session session, final MimeMultipart mimeMultipart, final EmailProperties properties) throws MessagingException {
        final List<InternetAddress> addresses = new ArrayList<>();
        try {
            addresses.add(new InternetAddress(emailAddress));
        } catch (final AddressException e) {
            logger.warn(String.format("Could not create the address from %s: %s", emailAddress, e.getMessage()));
        }

        if (addresses.isEmpty()) {
            throw new RuntimeException("There were no valid email addresses supplied.");
        }

        final Message message = new MimeMessage(session);
        message.setContent(mimeMultipart);

        message.setFrom(new InternetAddress(properties.getEmailFromAddress()));
        message.setRecipients(Message.RecipientType.TO, addresses.toArray(new Address[addresses.size()]));
        message.setSubject(subjectLine);

        return message;
    }

    public void sendMessage(final EmailProperties customerProperties, final Session session, final Message message) throws MessagingException {
        if (customerProperties.isAuth()) {
            sendAuthenticated(customerProperties, message, session);
        } else {
            Transport.send(message);
        }
    }

    private void sendAuthenticated(final EmailProperties customerProperties, final Message message, final Session session) throws MessagingException {
        final String host = customerProperties.getHost();
        final int port = customerProperties.getPort();
        final String username = customerProperties.getUsername();
        final String password = customerProperties.getPassword();

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
