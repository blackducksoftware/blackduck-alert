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
package com.blackducksoftware.integration.hub.alert.channel.email.template;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class EmailTargetTest {
    @Test
    public void targetTest() {
        final String emailAddress = "blah@blah.blah";
        final String templateName = "myTemplate";
        final Map<String, Object> model = new HashMap<>();

        model.put("example", new Object());

        final EmailTarget target = new EmailTarget(emailAddress, templateName, model);
        assertEquals(emailAddress, target.getEmailAddress());
        assertEquals(templateName, target.getTemplateName());
        assertEquals(model, target.getModel());
    }

}
