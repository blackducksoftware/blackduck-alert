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
package com.synopsys.integration.alert.service.email;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.api.common.model.AlertConstants;
import com.synopsys.integration.alert.test.common.TestResourceUtils;

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
