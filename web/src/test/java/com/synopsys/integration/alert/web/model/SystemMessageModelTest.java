package com.synopsys.integration.alert.web.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;

public class SystemMessageModelTest {

    @Test
    public void testContructor() {
        final String id = "1";
        final String severity = "type";
        final String createdAt = "createdAt";
        final String content = "content";
        final String type = "type";

        SystemMessageModel model = new SystemMessageModel(id, severity, createdAt, content, type);
        assertEquals(id, model.getId());
        assertEquals(severity, model.getSeverity());
        assertEquals(createdAt, model.getCreatedAt());
        assertEquals(content, model.getContent());
        assertEquals(type, model.getType());
    }
}
