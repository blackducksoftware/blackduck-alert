/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.service.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.common.model.AlertConstants;
import com.blackduck.integration.alert.test.common.TestResourceUtils;

import jakarta.mail.MessagingException;

public class MimeMultipartBuilderTest {
    @Test
    public void buildTest() throws MessagingException {
        final String html = "<html></html>";
        final String text = "content";
        List<String> attachmentFilePaths = new ArrayList<>();
        attachmentFilePaths.add(TestResourceUtils.DEFAULT_PROPERTIES_FILE_NAME);
        Map<String, String> contentIdsToFilePaths = new HashMap<>();
        contentIdsToFilePaths.put("entry", "synopsys.png");

        String originalValue = System.getProperty(AlertConstants.SYSTEM_PROPERTY_KEY_APP_HOME);
        try {
            System.setProperty(AlertConstants.SYSTEM_PROPERTY_KEY_APP_HOME, "src/main/resources/email");

            MimeMultipartBuilder builder = new MimeMultipartBuilder();
            builder.addHtmlContent(html);
            builder.addTextContent(text);
            builder.addAttachments(attachmentFilePaths);
            builder.addEmbeddedImages(contentIdsToFilePaths);

            builder.build();
        } finally {
            if (originalValue != null) {
                System.setProperty(AlertConstants.SYSTEM_PROPERTY_KEY_APP_HOME, originalValue);
            } else {
                System.clearProperty(AlertConstants.SYSTEM_PROPERTY_KEY_APP_HOME);
            }
        }
    }

}
