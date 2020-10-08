package com.synopsys.integration.alert.web.model;

import org.junit.Assert;
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
        Assert.assertEquals(id, model.getId());
        Assert.assertEquals(severity, model.getSeverity());
        Assert.assertEquals(createdAt, model.getCreatedAt());
        Assert.assertEquals(content, model.getContent());
        Assert.assertEquals(type, model.getType());
    }
}
