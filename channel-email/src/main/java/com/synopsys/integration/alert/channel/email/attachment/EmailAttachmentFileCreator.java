/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.attachment;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.attachment.compatibility.MessageContentGroup;
import com.synopsys.integration.alert.channel.email.distribution.ProjectMessageToMessageContentGroupConversionUtils;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.processor.api.extract.model.project.ProjectMessage;

@Component
public class EmailAttachmentFileCreator {
    private final Logger logger = LoggerFactory.getLogger(EmailAttachmentFileCreator.class);

    private final AlertProperties alertProperties;
    private final MessageContentGroupCsvCreator messageContentGroupCsvCreator;
    private final Gson gson;

    @Autowired
    public EmailAttachmentFileCreator(AlertProperties alertProperties, MessageContentGroupCsvCreator messageContentGroupCsvCreator, Gson gson) {
        this.alertProperties = alertProperties;
        this.messageContentGroupCsvCreator = messageContentGroupCsvCreator;
        this.gson = gson;
    }

    // TODO update in 7.0.0
    public Optional<File> createAttachmentFile(EmailAttachmentFormat attachmentFormat, ProjectMessage message) {
        if (EmailAttachmentFormat.NONE.equals(attachmentFormat)) {
            return Optional.empty();
        }

        MessageContentGroup messageContentGroup = ProjectMessageToMessageContentGroupConversionUtils.toMessageContentGroup(message);
        return createAttachmentFile(attachmentFormat, messageContentGroup);
    }

    // TODO remove in 7.0.0
    public Optional<File> createAttachmentFile(EmailAttachmentFormat attachmentFormat, MessageContentGroup message) {
        if (EmailAttachmentFormat.NONE.equals(attachmentFormat)) {
            return Optional.empty();
        }

        String alertEmailAttachmentsDirName = alertProperties.getAlertEmailAttachmentsDir();
        try {
            File emailAttachmentsDir = new File(alertEmailAttachmentsDirName);
            File formattedFile = createFormattedFile(attachmentFormat, message, emailAttachmentsDir);
            return Optional.ofNullable(formattedFile);
        } catch (SecurityException | IOException e) {
            logger.warn("Unable to create {} email attachment file", attachmentFormat.name());
            logger.debug("Attachment failure", e);
            return Optional.empty();
        }
    }

    public void cleanUpAttachmentFile(File attachmentFile) {
        if (null != attachmentFile && attachmentFile.exists()) {
            try {
                Files.delete(attachmentFile.toPath());
            } catch (SecurityException | IOException e) {
                logger.warn("Could not clean up file: {}", attachmentFile.getName());
                logger.debug("Attachment clean up failure", e);
            }
        }
    }

    private File createFormattedFile(EmailAttachmentFormat attachmentFormat, MessageContentGroup messageContentGroup, File writeDir) throws IOException {
        switch (attachmentFormat) {
            case CSV:
                return createCsvFile(messageContentGroup, writeDir);
            case JSON:
                return createJsonFile(messageContentGroup, writeDir);
            case XML:
                return createXmlFile(messageContentGroup, writeDir);
            default:
                return null;
        }
    }

    private File createJsonFile(MessageContentGroup messageContentGroup, File writeDir) throws IOException {
        String jsonMessage = gson.toJson(messageContentGroup);
        return createFile(jsonMessage, writeDir, "json");
    }

    private File createCsvFile(MessageContentGroup messageContentGroup, File writeDir) throws IOException {
        String csvMessage = messageContentGroupCsvCreator.createCsvString(messageContentGroup);
        return createFile(csvMessage, writeDir, "csv");
    }

    private File createXmlFile(MessageContentGroup messageContentGroup, File writeDir) throws IOException {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(new Jdk8Module());

        String xmlMessage = xmlMapper.writeValueAsString(messageContentGroup);
        return createFile(xmlMessage, writeDir, "xml");
    }

    private File createFile(String messageString, File writeDir, String fileExtension) throws IOException {
        File messageFile = new File(writeDir, "message." + fileExtension);
        FileUtils.writeStringToFile(messageFile, messageString, Charset.defaultCharset());
        return messageFile;
    }

}
