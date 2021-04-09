package com.synopsys.integration.alert.database.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
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
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

@Transactional
@AlertIntegrationTest
public class SystemMessageAccessorTestIT {
    public static final String SEVERITY = "severity";
    public static final String TYPE = "type";
    private static final int MESSAGE_COUNT = 5;

    @Autowired
    private DefaultSystemMessageAccessor defaultSystemMessageUtility;
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
        List<SystemMessageEntity> expectedMessageList = createSystemMessageList();
        systemMessageRepository.saveAll(expectedMessageList);
        List<SystemMessageModel> actualMessageList = defaultSystemMessageUtility.getSystemMessages();
        assertEquals(expectedMessageList.size(), actualMessageList.size());
    }

    @Test
    public void testAddSystemMessage() {
        final String content = "add message test content";
        final SystemMessageSeverity systemMessageSeverity = SystemMessageSeverity.WARNING;
        defaultSystemMessageUtility.addSystemMessage(content, systemMessageSeverity, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        List<SystemMessageEntity> actualMessageList = systemMessageRepository.findAll();
        assertEquals(1, actualMessageList.size());
        SystemMessageEntity actualMessage = actualMessageList.get(0);
        assertEquals(content, actualMessage.getContent());
        assertEquals(systemMessageSeverity.name(), actualMessage.getSeverity());
    }

    @Test
    public void testRemoveSystemMessagesByType() {
        List<SystemMessageEntity> expectedMessages = createSystemMessageList();
        systemMessageRepository.saveAll(expectedMessages);
        final SystemMessageSeverity systemMessageSeverity = SystemMessageSeverity.WARNING;
        defaultSystemMessageUtility.addSystemMessage("message 1", systemMessageSeverity, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        defaultSystemMessageUtility.addSystemMessage("message 2", systemMessageSeverity, SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        List<SystemMessageEntity> savedMessages = systemMessageRepository.findAll();
        assertEquals(MESSAGE_COUNT + 2, savedMessages.size());
        defaultSystemMessageUtility.removeSystemMessagesByType(SystemMessageType.ENCRYPTION_CONFIGURATION_ERROR);
        List<SystemMessageEntity> actualMessageList = systemMessageRepository.findAll();
        assertNotNull(actualMessageList);
        assertEquals(MESSAGE_COUNT, actualMessageList.size());
        assertEquals(expectedMessages, actualMessageList);
    }

    @Test
    public void testGetSystemMessagesSince() {
        List<SystemMessageEntity> savedMessages = createSystemMessageList();
        OffsetDateTime currentDateTime = DateUtils.createCurrentDateTimestamp();
        savedMessages.add(new SystemMessageEntity(currentDateTime.plusNanos(1), SEVERITY, "content", TYPE));
        savedMessages.add(new SystemMessageEntity(currentDateTime.plusNanos(5), SEVERITY, "content", TYPE));
        systemMessageRepository.saveAll(savedMessages);

        List<SystemMessageModel> actualMessageList = defaultSystemMessageUtility.getSystemMessagesAfter(currentDateTime);
        assertNotNull(actualMessageList);
        assertEquals(2, actualMessageList.size());
    }

    @Test
    public void testFindCreatedBefore() {
        List<SystemMessageEntity> expectedMessages = createSystemMessageList();
        Collections.reverse(expectedMessages);
        List<SystemMessageEntity> savedMessages = new ArrayList<>(expectedMessages);
        OffsetDateTime currentDateTime = DateUtils.createCurrentDateTimestamp();
        savedMessages.add(new SystemMessageEntity(currentDateTime, SEVERITY, "content", TYPE));
        savedMessages.add(new SystemMessageEntity(currentDateTime.plusMinutes(5), SEVERITY, "content", TYPE));
        systemMessageRepository.saveAll(savedMessages);
        List<SystemMessageModel> actualMessageList = defaultSystemMessageUtility.getSystemMessagesBefore(currentDateTime);
        assertNotNull(actualMessageList);
        assertEquals(MESSAGE_COUNT, actualMessageList.size());
        assertEquals(expectedMessages.size(), actualMessageList.size());
    }

    @Test
    public void testFindCreatedBeforeEmptyList() {
        OffsetDateTime currentDate = DateUtils.createCurrentDateTimestamp();
        List<SystemMessageModel> actualMessageList = defaultSystemMessageUtility.getSystemMessagesBefore(currentDate);
        assertTrue(actualMessageList.isEmpty());
    }

    @Test
    public void testFindBetweenDateRange() {
        List<SystemMessageEntity> expectedMessages = createSystemMessageList();
        Collections.reverse(expectedMessages);
        OffsetDateTime currentDateTime = DateUtils.createCurrentDateTimestamp();
        OffsetDateTime startTime = currentDateTime.minusMinutes(10);
        List<SystemMessageEntity> savedMessages = new ArrayList<>(expectedMessages);
        savedMessages.add(new SystemMessageEntity(currentDateTime, SEVERITY, "content", TYPE));
        savedMessages.add(new SystemMessageEntity(startTime.minusMinutes(15), SEVERITY, "content", TYPE));
        savedMessages.add(new SystemMessageEntity(currentDateTime.plusMinutes(5), SEVERITY, "content", TYPE));
        systemMessageRepository.saveAll(savedMessages);
        DateRange dateRange = DateRange.of(startTime, currentDateTime);
        List<SystemMessageModel> actualMessageList = defaultSystemMessageUtility.findBetween(dateRange);
        assertNotNull(actualMessageList);
        assertEquals(MESSAGE_COUNT, actualMessageList.size());
        assertEquals(expectedMessages.size(), actualMessageList.size());
    }

    @Test
    public void testDeleteList() {
        List<SystemMessageEntity> savedMessages = createSystemMessageList();
        systemMessageRepository.saveAll(savedMessages);
        List<SystemMessageModel> messagesToDelete = savedMessages.subList(1, 3).stream().map(this::convertToSystemMessage).collect(Collectors.toList());

        defaultSystemMessageUtility.deleteSystemMessages(messagesToDelete);

        List<SystemMessageEntity> actualMessageList = systemMessageRepository.findAll();
        assertNotEquals(savedMessages.stream().map(this::convertToSystemMessage).collect(Collectors.toList()), actualMessageList);
        assertNotEquals(messagesToDelete.size(), actualMessageList.size());
    }

    private List<SystemMessageEntity> createSystemMessageList() {
        List<SystemMessageEntity> messages = new ArrayList<>(MESSAGE_COUNT);
        OffsetDateTime dateTime = DateUtils.createCurrentDateTimestamp();
        for (int index = 0; index < MESSAGE_COUNT; index++) {
            dateTime = dateTime.minusMinutes(1);
            messages.add(new SystemMessageEntity(dateTime, "severity_" + index, "content_" + index, TYPE + "_" + index));
        }
        return messages;
    }

    private SystemMessageModel convertToSystemMessage(SystemMessageEntity systemMessage) {
        String date = DateUtils.formatDateAsJsonString(systemMessage.getCreated());
        return new SystemMessageModel(String.valueOf(systemMessage.getId()), date, systemMessage.getSeverity(), systemMessage.getContent(), systemMessage.getType());
    }

}
