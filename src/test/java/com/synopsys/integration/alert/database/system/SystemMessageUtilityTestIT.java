package com.synopsys.integration.alert.database.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.util.AlertIntegrationTest;
import com.synopsys.integration.rest.RestConstants;

@Transactional
public class SystemMessageUtilityTestIT extends AlertIntegrationTest {
    public static final String SEVERITY = "severity";
    public static final String TYPE = "type";
    private static final int MESSAGE_COUNT = 5;
    @Autowired
    private DefaultSystemMessageUtility defaultSystemMessageUtility;
    @Autowired
    private SystemMessageRepository systemMessageRepository;

    @BeforeEach
    public void init() {
        systemMessageRepository.deleteAllInBatch();
        systemMessageRepository.flush();
    }

    @AfterEach
    public void cleanup() {
        systemMessageRepository.flush();
        systemMessageRepository.deleteAllInBatch();
    }

    @Test
    public void testGetSystemMessages() {
        List<SystemMessage> expectedMessageList = createSystemMessageList();
        systemMessageRepository.saveAll(expectedMessageList);
        List<SystemMessageModel> actualMessageList = defaultSystemMessageUtility.getSystemMessages();
        assertEquals(expectedMessageList.size(), actualMessageList.size());
    }

    @Test
    public void testAddSystemMessage() {
        final String content = "add message test content";
        final SystemMessageSeverity systemMessageSeverity = SystemMessageSeverity.WARNING;
        defaultSystemMessageUtility.addSystemMessage(content, systemMessageSeverity, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        List<SystemMessage> actualMessageList = systemMessageRepository.findAll();
        assertEquals(1, actualMessageList.size());
        SystemMessage actualMessage = actualMessageList.get(0);
        assertEquals(content, actualMessage.getContent());
        assertEquals(systemMessageSeverity.name(), actualMessage.getSeverity());
    }

    @Test
    public void testRemoveSystemMessagesByType() {
        List<SystemMessage> expectedMessages = createSystemMessageList();
        systemMessageRepository.saveAll(expectedMessages);
        final SystemMessageSeverity systemMessageSeverity = SystemMessageSeverity.WARNING;
        defaultSystemMessageUtility.addSystemMessage("message 1", systemMessageSeverity, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        defaultSystemMessageUtility.addSystemMessage("message 2", systemMessageSeverity, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        List<SystemMessage> savedMessages = systemMessageRepository.findAll();
        assertEquals(MESSAGE_COUNT + 2, savedMessages.size());
        defaultSystemMessageUtility.removeSystemMessagesByType(SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        List<SystemMessage> actualMessageList = systemMessageRepository.findAll();
        assertNotNull(actualMessageList);
        assertEquals(MESSAGE_COUNT, actualMessageList.size());
        assertEquals(expectedMessages, actualMessageList);
    }

    @Test
    public void testGetSystemMessagesSince() {
        List<SystemMessage> savedMessages = createSystemMessageList();
        ZonedDateTime currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        Date currentDate = Date.from(currentTime.toInstant());
        savedMessages.add(new SystemMessage(currentDate, SEVERITY, "content", TYPE));
        currentTime.plusMinutes(5);
        savedMessages.add(new SystemMessage(Date.from(currentTime.toInstant()), SEVERITY, "content", TYPE));
        systemMessageRepository.saveAll(savedMessages);
        List<SystemMessageModel> actualMessageList = defaultSystemMessageUtility.getSystemMessagesAfter(currentDate);
        assertNotNull(actualMessageList);
        assertEquals(2, actualMessageList.size());
    }

    @Test
    public void testFindCreatedBefore() {
        List<SystemMessage> expectedMessages = createSystemMessageList();
        Collections.reverse(expectedMessages);
        ZonedDateTime currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        List<SystemMessage> savedMessages = new ArrayList<>(expectedMessages);
        Date currentDate = Date.from(currentTime.toInstant());
        savedMessages.add(new SystemMessage(currentDate, SEVERITY, "content", TYPE));
        currentTime = currentTime.plusMinutes(5);
        savedMessages.add(new SystemMessage(Date.from(currentTime.toInstant()), SEVERITY, "content", TYPE));
        systemMessageRepository.saveAll(savedMessages);
        List<SystemMessageModel> actualMessageList = defaultSystemMessageUtility.getSystemMessagesBefore(currentDate);
        assertNotNull(actualMessageList);
        assertEquals(MESSAGE_COUNT, actualMessageList.size());
        assertEquals(expectedMessages.size(), actualMessageList.size());
    }

    @Test
    public void testFindCreatedBeforeEmptyList() {
        ZonedDateTime currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        Date currentDate = Date.from(currentTime.toInstant());
        List<SystemMessageModel> actualMessageList = defaultSystemMessageUtility.getSystemMessagesBefore(currentDate);
        assertTrue(actualMessageList.isEmpty());
    }

    @Test
    public void testFindBetweenDateRange() {
        List<SystemMessage> expectedMessages = createSystemMessageList();
        Collections.reverse(expectedMessages);
        ZonedDateTime currentTime = ZonedDateTime.now().withZoneSameInstant(ZoneOffset.UTC);
        ZonedDateTime startTime = currentTime.minusMinutes(10);
        List<SystemMessage> savedMessages = new ArrayList<>(expectedMessages);
        Date currentDate = Date.from(currentTime.toInstant());
        savedMessages.add(new SystemMessage(currentDate, SEVERITY, "content", TYPE));
        currentTime = currentTime.plusMinutes(5);
        savedMessages.add(new SystemMessage(Date.from(startTime.minusMinutes(15).toInstant()), SEVERITY, "content", TYPE));
        savedMessages.add(new SystemMessage(Date.from(currentTime.toInstant()), SEVERITY, "content", TYPE));
        systemMessageRepository.saveAll(savedMessages);
        DateRange dateRange = DateRange.of(Date.from(startTime.toInstant()), currentDate);
        List<SystemMessageModel> actualMessageList = defaultSystemMessageUtility.findBetween(dateRange);
        assertNotNull(actualMessageList);
        assertEquals(MESSAGE_COUNT, actualMessageList.size());
        assertEquals(expectedMessages.size(), actualMessageList.size());
    }

    @Test
    public void testDeleteList() {
        List<SystemMessage> savedMessages = createSystemMessageList();
        systemMessageRepository.saveAll(savedMessages);
        List<SystemMessageModel> messagesToDelete = savedMessages.subList(1, 3).stream().map(this::convertToSystemMessage).collect(Collectors.toList());

        defaultSystemMessageUtility.deleteSystemMessages(messagesToDelete);

        List<SystemMessage> actualMessageList = systemMessageRepository.findAll();
        assertNotEquals(savedMessages.stream().map(this::convertToSystemMessage).collect(Collectors.toList()), actualMessageList);
        assertNotEquals(messagesToDelete.size(), actualMessageList.size());
    }

    private List<SystemMessage> createSystemMessageList() {
        List<SystemMessage> messages = new ArrayList<>(5);
        ZonedDateTime zonedDateTime = ZonedDateTime.now();
        for (int index = 0; index < MESSAGE_COUNT; index++) {
            zonedDateTime = zonedDateTime.minusMinutes(1);
            messages.add(new SystemMessage(Date.from(zonedDateTime.toInstant()), "severity_" + index, "content_" + index, TYPE + "_" + index));
        }
        return messages;
    }

    private SystemMessageModel convertToSystemMessage(SystemMessage systemMessage) {
        String date = RestConstants.formatDate(systemMessage.getCreated());
        return new SystemMessageModel(date, systemMessage.getSeverity(), systemMessage.getContent(), systemMessage.getType());
    }
}
