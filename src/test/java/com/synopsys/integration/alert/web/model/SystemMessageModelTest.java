package com.synopsys.integration.alert.web.model;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class SystemMessageModelTest {

    @Test
    public void testContructor() {
        final String type = "type";
        final String createdAt = "createdAt";
        final String content = "content";

        final SystemMessageModel model = new SystemMessageModel(type, createdAt, content);
        assertEquals(type, model.getType());
        assertEquals(createdAt, model.getCreatedAt());
        assertEquals(content, model.getContent());
    }
}
