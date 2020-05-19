package com.synopsys.integration.alert.database.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.util.DateUtils;

public class SystemMessageTest {
    @Test
    public void testConstructor() {
        SystemMessageEntity systemMessage = new SystemMessageEntity();
        assertNull(systemMessage.getContent());
        assertNull(systemMessage.getCreated());
        assertNull(systemMessage.getSeverity());
        assertNull(systemMessage.getType());
    }

    @Test
    public void testGetters() {
        OffsetDateTime date = DateUtils.createCurrentDateTimestamp();
        final String severity = "severity";
        final String content = "contents";
        final String type = "type";
        SystemMessageEntity systemMessage = new SystemMessageEntity(date, severity, content, type);
        assertEquals(date, systemMessage.getCreated());
        assertEquals(severity, systemMessage.getSeverity());
        assertEquals(content, systemMessage.getContent());
        assertEquals(type, systemMessage.getType());
    }

}
