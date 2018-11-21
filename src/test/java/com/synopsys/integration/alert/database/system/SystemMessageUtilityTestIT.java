package com.synopsys.integration.alert.database.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.AlertIntegrationTest;
import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.model.DateRange;

public class SystemMessageUtilityTestIT extends AlertIntegrationTest {
    private static final int MESSAGE_COUNT = 5;
    public static final String SEVERITY = "severity";
    public static final String TYPE = "type";
    @Autowired
    private SystemMessageUtility systemMessageUtility;
    @Autowired
    private SystemMessageRepository systemMessageRepository;

    @Before
    public void initializeTests() {
        systemMessageRepository.deleteAll();
    }

    @Test
    public void testGetSystemMessages() {
        final List<SystemMessage> expectedMessageList = createSystemMessageList();
        systemMessageRepository.saveAll(expectedMessageList);
        final List<SystemMessage> actualMessageList = systemMessageUtility.getSystemMessages();
        assertEquals(expectedMessageList, actualMessageList);
    }

    @Test
    public void testAddSystemMessage() {
        final String content = "add message test content";
        final SystemMessageSeverity systemMessageSeverity = SystemMessageSeverity.WARNING;
        systemMessageUtility.addSystemMessage(content, systemMessageSeverity, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        final List<SystemMessage> actualMessageList = systemMessageRepository.findAll();
        assertEquals(1, actualMessageList.size());
        final SystemMessage actualMessage = actualMessageList.get(0);
        assertEquals(content, actualMessage.getContent());
        assertEquals(systemMessageSeverity.name(), actualMessage.getSeverity());
    }

    @Test
    public void testRemoveSystemMessagesByType() {
        final List<SystemMessage> expectedMessages = createSystemMessageList();
        systemMessageRepository.saveAll(expectedMessages);
        final SystemMessageSeverity systemMessageSeverity = SystemMessageSeverity.WARNING;
        systemMessageUtility.addSystemMessage("message 1", systemMessageSeverity, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        systemMessageUtility.addSystemMessage("message 2", systemMessageSeverity, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        final List<SystemMessage> savedMessages = systemMessageRepository.findAll();
        assertEquals(MESSAGE_COUNT + 2, savedMessages.size());
        systemMessageUtility.removeSystemMessagesByType(SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        final List<SystemMessage> actualMessageList = systemMessageRepository.findAll();
        assertNotNull(actualMessageList);
        assertEquals(MESSAGE_COUNT, actualMessageList.size());
        assertEquals(expectedMessages, actualMessageList);
    }

    @Test
    public void testGetSystemMessagesSince() {
        final List<SystemMessage> savedMessages = createSystemMessageList();
        final ZonedDateTime currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        final Date currentDate = Date.from(currentTime.toInstant());
        savedMessages.add(new SystemMessage(currentDate, SEVERITY, "content", TYPE));
        currentTime.plusMinutes(5);
        savedMessages.add(new SystemMessage(Date.from(currentTime.toInstant()), SEVERITY, "content", TYPE));
        systemMessageRepository.saveAll(savedMessages);
        final List<SystemMessage> actualMessageList = systemMessageUtility.getSystemMessagesAfter(currentDate);
        assertNotNull(actualMessageList);
        assertEquals(2, actualMessageList.size());
    }

    @Test
    public void testFindCreatedBefore() {
        final List<SystemMessage> expectedMessages = createSystemMessageList();
        Collections.reverse(expectedMessages);
        ZonedDateTime currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        final List<SystemMessage> savedMessages = new ArrayList<>(expectedMessages);
        final Date currentDate = Date.from(currentTime.toInstant());
        savedMessages.add(new SystemMessage(currentDate, SEVERITY, "content", TYPE));
        currentTime = currentTime.plusMinutes(5);
        savedMessages.add(new SystemMessage(Date.from(currentTime.toInstant()), SEVERITY, "content", TYPE));
        systemMessageRepository.saveAll(savedMessages);
        final List<SystemMessage> actualMessageList = systemMessageUtility.getSystemMessagesBefore(currentDate);
        assertNotNull(actualMessageList);
        assertEquals(MESSAGE_COUNT, actualMessageList.size());
        assertEquals(expectedMessages, actualMessageList);
    }

    @Test
    public void testFindBetweenDateRange() {
        final List<SystemMessage> expectedMessages = createSystemMessageList();
        Collections.reverse(expectedMessages);
        ZonedDateTime currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        final ZonedDateTime startTime = currentTime.minusMinutes(10);
        final List<SystemMessage> savedMessages = new ArrayList<>(expectedMessages);
        final Date currentDate = Date.from(currentTime.toInstant());
        savedMessages.add(new SystemMessage(currentDate, SEVERITY, "content", TYPE));
        currentTime = currentTime.plusMinutes(5);
        savedMessages.add(new SystemMessage(Date.from(startTime.minusMinutes(15).toInstant()), SEVERITY, "content", TYPE));
        savedMessages.add(new SystemMessage(Date.from(currentTime.toInstant()), SEVERITY, "content", TYPE));
        systemMessageRepository.saveAll(savedMessages);
        final DateRange dateRange = DateRange.of(Date.from(startTime.toInstant()), currentDate);
        final List<SystemMessage> actualMessageList = systemMessageUtility.findBetween(dateRange);
        assertNotNull(actualMessageList);
        assertEquals(MESSAGE_COUNT, actualMessageList.size());
        assertEquals(expectedMessages, actualMessageList);
    }

    @Test
    public void testDeleteList() {
        final List<SystemMessage> savedMessages = createSystemMessageList();
        systemMessageRepository.saveAll(savedMessages);
        final List<SystemMessage> messagesToDelete = savedMessages.subList(1, 3);

        systemMessageUtility.deleteSystemMessages(messagesToDelete);

        final List<SystemMessage> actualMessageList = systemMessageRepository.findAll();
        assertNotEquals(savedMessages, actualMessageList);
        assertNotEquals(messagesToDelete.size(), actualMessageList.size());
    }

    private List<SystemMessage> createSystemMessageList() {
        final List<SystemMessage> messages = new ArrayList<>(5);
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        for (int index = 0; index < MESSAGE_COUNT; index++) {
            zonedDateTime = zonedDateTime.minusMinutes(1);
            messages.add(new SystemMessage(Date.from(zonedDateTime.toInstant()), "severity_" + index, "content_" + index, TYPE + "_" + index));
        }
        return messages;
    }
}
