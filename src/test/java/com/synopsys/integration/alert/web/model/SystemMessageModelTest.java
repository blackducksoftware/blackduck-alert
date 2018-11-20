package com.synopsys.integration.alert.web.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SystemMessageModelTest {

    @Test
    public void testContructor() {
        final String severity = "type";
        final String createdAt = "createdAt";
        final String content = "content";
        final String type = "type";

        final SystemMessageModel model = new SystemMessageModel(severity, createdAt, content, type);
        assertEquals(severity, model.getSeverity());
        assertEquals(createdAt, model.getCreatedAt());
        assertEquals(content, model.getContent());
        assertEquals(type, model.getType());
    }
}
