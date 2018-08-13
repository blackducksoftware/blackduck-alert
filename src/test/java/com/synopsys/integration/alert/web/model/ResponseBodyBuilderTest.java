/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.web.model;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class ResponseBodyBuilderTest {

    @Test
    public void testResponseBodyBuilderEmpty() {
        final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(null, null);
        assertEquals("{\"id\":null,\"message\":null}", responseBodyBuilder.build());
        assertEquals("{\"id\":null,\"message\":null}", responseBodyBuilder.toString());
    }

    @Test
    public void testResponseBodyBuilder() {
        final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(55L, "Message");

        responseBodyBuilder.put("Key1", "Value");
        responseBodyBuilder.put("Key2", 22);
        responseBodyBuilder.put("Key3", false);

        assertEquals("{\"id\":55,\"message\":\"Message\",\"Key1\":\"Value\",\"Key2\":22,\"Key3\":false}", responseBodyBuilder.build());
    }

    @Test
    public void testResponseBodyBuilderErrors() {
        final ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(33L, "There were errors");

        final Map<String, String> errors = new HashMap<>();
        errors.put("Field", "Terrible error");

        responseBodyBuilder.putErrors(errors);

        assertEquals("{\"id\":33,\"message\":\"There were errors\",\"errors\":{\"Field\":\"Terrible error\"}}", responseBodyBuilder.build());
    }
}
