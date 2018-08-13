/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.channel.email.template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.junit.Test;

import com.synopsys.integration.alert.ResourceLoader;
import com.synopsys.integration.alert.AlertConstants;

public class MimeMultipartBuilderTest {
    @Test
    public void buildTest() throws MessagingException {
        final String html = "<html></html>";
        final String text = "content";
        final List<String> attachmentFilePaths = new ArrayList<>();
        attachmentFilePaths.add(ResourceLoader.DEFAULT_PROPERTIES_FILE_LOCATION);
        final Map<String, String> contentIdsToFilePaths = new HashMap<>();
        contentIdsToFilePaths.put("entry", "Ducky-80.jpg");

        final String originalValue = System.getProperty(AlertConstants.SYSTEM_PROPERTY_KEY_APP_HOME);
        try {
            System.setProperty(AlertConstants.SYSTEM_PROPERTY_KEY_APP_HOME, "src/main/resources/email");

            final MimeMultipartBuilder builder = new MimeMultipartBuilder();
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
