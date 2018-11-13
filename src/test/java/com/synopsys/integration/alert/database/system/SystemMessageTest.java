package com.synopsys.integration.alert.database.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.junit.Test;

public class SystemMessageTest {

    @Test
    public void testConstructor() {
        final SystemMessage systemMessage = new SystemMessage();
        assertNull(systemMessage.getContent());
        assertNull(systemMessage.getCreated());
        assertNull(systemMessage.getType());
    }

    @Test
    public void testGetters() {
        final Date date = new Date();
        final String type = "type";
        final String content = "contents";
        final SystemMessage systemMessage = new SystemMessage(date, type, content);
        assertEquals(date, systemMessage.getCreated());
        assertEquals(type, systemMessage.getType());
        assertEquals(content, systemMessage.getContent());
    }
}
