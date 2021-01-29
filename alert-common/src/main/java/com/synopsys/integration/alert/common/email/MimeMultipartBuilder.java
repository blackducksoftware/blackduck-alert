/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.email;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.AlertConstants;

public class MimeMultipartBuilder {
    private String html;

    private String text;

    private final Map<String, String> contentIdsToFilePaths = new HashMap<>();

    private final List<String> attachmentFilePaths = new ArrayList<>();

    public MimeMultipart build() throws MessagingException {
        final MimeMultipart email = new MimeMultipart("mixed");

        final MimeBodyPart emailBodyPart = buildEmailBodyPart();
        email.addBodyPart(emailBodyPart);
        addAttachmentBodyParts(email);

        return email;
    }

    public void addHtmlContent(final String html) {
        this.html = html;
    }

    public void addTextContent(final String text) {
        this.text = text;
    }

    public void addEmbeddedImages(final Map<String, String> contentIdsToFilePaths) {
        this.contentIdsToFilePaths.putAll(contentIdsToFilePaths);
    }

    public void addAttachments(final List<String> attachmentFilePaths) {
        this.attachmentFilePaths.addAll(attachmentFilePaths);
    }

    private MimeBodyPart buildEmailBodyPart() throws MessagingException {
        final MimeMultipart emailContent = new MimeMultipart("alternative");

        // add from low fidelity to high fidelity
        if (StringUtils.isNotBlank(text)) {
            final MimeBodyPart textBodyPart = buildTextBodyPart();
            emailContent.addBodyPart(textBodyPart);
        }

        if (StringUtils.isNotBlank(html)) {
            final MimeBodyPart htmlBodyPart = buildHtmlBodyPart();
            emailContent.addBodyPart(htmlBodyPart);
        }

        final MimeBodyPart emailBodyPart = new MimeBodyPart();
        emailBodyPart.setContent(emailContent);
        return emailBodyPart;
    }

    private MimeBodyPart buildHtmlBodyPart() throws MessagingException {
        final MimeMultipart htmlContent = new MimeMultipart("related");

        final MimeBodyPart htmlPart = new MimeBodyPart();
        htmlPart.setContent(html, "text/html; charset=utf-8");
        htmlContent.addBodyPart(htmlPart);

        for (final Map.Entry<String, String> entry : contentIdsToFilePaths.entrySet()) {
            final MimeBodyPart embeddedImageBodyPart = new MimeBodyPart();
            String imageFilePath = entry.getValue();
            File imageFile = new File(imageFilePath);

            if (!imageFile.exists()) {
                try {
                    final File imagesDir = findImagesDirectory();
                    if (imagesDir != null) {
                        imageFile = new File(imagesDir, imageFilePath);
                        if (imageFile.exists()) {
                            imageFilePath = imageFile.getCanonicalPath();

                        }
                    }
                } catch (final Exception e) {
                    // ignore let freemarker fail and log the exception
                    // up the chain when it cannot find the image file
                }
            }
            final DataSource fds = new FileDataSource(imageFilePath);
            embeddedImageBodyPart.setDataHandler(new DataHandler(fds));
            embeddedImageBodyPart.setHeader("Content-ID", entry.getKey());
            htmlContent.addBodyPart(embeddedImageBodyPart);
        }

        final MimeBodyPart htmlBodyPart = new MimeBodyPart();
        htmlBodyPart.setContent(htmlContent);
        return htmlBodyPart;
    }

    private MimeBodyPart buildTextBodyPart() throws MessagingException {
        final MimeBodyPart textPart = new MimeBodyPart();
        textPart.setText(text, "utf-8");
        return textPart;
    }

    private void addAttachmentBodyParts(final MimeMultipart email) throws MessagingException {
        for (final String filePath : attachmentFilePaths) {
            final MimeBodyPart attachmentBodyPart = new MimeBodyPart();
            final DataSource source = new FileDataSource(filePath);
            attachmentBodyPart.setDataHandler(new DataHandler(source));
            attachmentBodyPart.setFileName(FilenameUtils.getName(filePath));
            email.addBodyPart(attachmentBodyPart);
        }
    }

    private File findImagesDirectory() {
        File imagesDir = null;
        final String appHomeDir = System.getProperty(AlertConstants.SYSTEM_PROPERTY_KEY_APP_HOME);
        if (StringUtils.isNotBlank(appHomeDir)) {
            imagesDir = new File(appHomeDir, "images");
        }
        return imagesDir;
    }
}
