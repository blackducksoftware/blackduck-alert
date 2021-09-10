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
