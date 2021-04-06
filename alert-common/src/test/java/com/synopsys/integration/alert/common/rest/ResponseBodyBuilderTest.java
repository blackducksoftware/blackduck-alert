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
package com.synopsys.integration.alert.common.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;

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
