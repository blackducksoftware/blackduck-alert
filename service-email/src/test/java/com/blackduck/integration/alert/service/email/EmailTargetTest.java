/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.service.email;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

public class EmailTargetTest {
    @Test
    public void targetTest() {
        final String emailAddress = "blah@blah.blah";
        final String templateName = "myTemplate";
        Map<String, Object> model = new HashMap<>();
        Map<String, String> contentIdsToFilePaths = new HashMap<>();

        model.put("example", new Object());
        contentIdsToFilePaths.put("test", "value");

        EmailTarget target = new EmailTarget(emailAddress, templateName, model, contentIdsToFilePaths);
        assertEquals(emailAddress, target.getEmailAddresses().stream().findFirst().orElseThrow());
        assertEquals(templateName, target.getTemplateName());
        assertEquals(model, target.getModel());
        assertEquals(contentIdsToFilePaths, target.getContentIdsToFilePaths());
    }

}
