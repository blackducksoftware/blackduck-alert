/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;

public class ResponseBodyBuilderTest {

    @Test
    public void testResponseBodyBuilderEmpty() {
        ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder(null, null);
        assertEquals("{\"id\":null,\"message\":null}", responseBodyBuilder.build());
        assertEquals("{\"id\":null,\"message\":null}", responseBodyBuilder.toString());
    }

    @Test
    public void testResponseBodyBuilder() {
        ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder("55L", "Message");

        responseBodyBuilder.put("Key1", "Value");
        responseBodyBuilder.put("Key2", 22);
        responseBodyBuilder.put("Key3", false);

        assertEquals("{\"id\":\"55L\",\"message\":\"Message\",\"Key1\":\"Value\",\"Key2\":22,\"Key3\":false}", responseBodyBuilder.build());
    }

    @Test
    public void testResponseBodyBuilderErrors() {
        ResponseBodyBuilder responseBodyBuilder = new ResponseBodyBuilder("33L", "There were errors");

        List<AlertFieldStatus> errors = new ArrayList<>();
        errors.add(AlertFieldStatus.error("Field", "Terrible error"));

        responseBodyBuilder.putErrors(errors);

        assertEquals("{\"id\":\"33L\",\"message\":\"There were errors\",\"errors\":{\"Field\":{\"severity\":\"ERROR\",\"fieldMessage\":\"Terrible error\"}}}", responseBodyBuilder.build());
    }

}
