/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.database.system;

import java.text.ParseException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageAccessor;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.common.util.DateUtils;

@Component
@Transactional
public class DefaultSystemMessageAccessor implements SystemMessageAccessor {
    private final Logger logger = LoggerFactory.getLogger(DefaultSystemMessageAccessor.class);
    private final SystemMessageRepository systemMessageRepository;

    @Autowired
    public DefaultSystemMessageAccessor(SystemMessageRepository systemMessageRepository) {
        this.systemMessageRepository = systemMessageRepository;
    }

    @Override
    public void addSystemMessage(String message, SystemMessageSeverity severity, SystemMessageType messageType) {
        addSystemMessage(message, severity, messageType.name());
    }

    @Override
    public void addSystemMessage(String message, SystemMessageSeverity severity, String messageType) {
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        SystemMessageEntity systemMessage = new SystemMessageEntity(currentTime, severity.name(), message, messageType);
        systemMessageRepository.save(systemMessage);
    }

    @Override
    public void removeSystemMessagesByType(SystemMessageType messageType) {
        removeSystemMessagesByTypeString(messageType.name());
    }

    @Override
    public void removeSystemMessagesByTypeString(String systemMessageType) {
        List<SystemMessageEntity> messages = systemMessageRepository.findByType(systemMessageType);
        systemMessageRepository.deleteAll(messages);
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<SystemMessageModel> getSystemMessages() {
        return convertAllToSystemMessageModel(systemMessageRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<SystemMessageModel> getSystemMessagesAfter(OffsetDateTime date) {
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        return convertAllToSystemMessageModel(systemMessageRepository.findByCreatedBetween(date, currentTime));
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<SystemMessageModel> getSystemMessagesBefore(OffsetDateTime date) {
        long recordCount = systemMessageRepository.count();
        if (recordCount == 0) {
            return Collections.emptyList();
        } else {
            SystemMessageEntity oldestMessage = systemMessageRepository.findTopByOrderByCreatedAsc();
            return convertAllToSystemMessageModel(systemMessageRepository.findByCreatedBetween(oldestMessage.getCreated(), date));
        }
    }

    @Override
    @Transactional(readOnly = true, isolation = Isolation.READ_COMMITTED)
    public List<SystemMessageModel> findBetween(DateRange dateRange) {
        return systemMessageRepository.findByCreatedBetween(dateRange.getStart(), dateRange.getEnd()).stream().map(this::convertToSystemMessageModel).collect(Collectors.toList());
    }

    @Override
    public void deleteSystemMessages(List<SystemMessageModel> messagesToDelete) {
        List<SystemMessageEntity> convertedMessages = messagesToDelete.stream()
            .map(this::convertToSystemMessage)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        systemMessageRepository.deleteAll(convertedMessages);
    }

    @Override
    public int deleteSystemMessagesCreatedBefore(OffsetDateTime date) {
        return systemMessageRepository.bulkDeleteCreatedBefore(date);
    }

    private List<SystemMessageModel> convertAllToSystemMessageModel(List<SystemMessageEntity> systemMessages) {
        return systemMessages
            .stream()
            .map(this::convertToSystemMessageModel)
            .collect(Collectors.toList());
    }

    private SystemMessageModel convertToSystemMessageModel(SystemMessageEntity systemMessage) {
        String createdAt = DateUtils.formatDateAsJsonString(systemMessage.getCreated());
        return new SystemMessageModel(String.valueOf(systemMessage.getId()), systemMessage.getSeverity(), createdAt, systemMessage.getContent(), systemMessage.getType());
    }

    private SystemMessageEntity convertToSystemMessage(SystemMessageModel systemMessageModel) {
        try {
            OffsetDateTime date = DateUtils.parseDateFromJsonString(systemMessageModel.getCreatedAt());
            SystemMessageEntity entity = new SystemMessageEntity(date, systemMessageModel.getSeverity(), systemMessageModel.getContent(), systemMessageModel.getType());
            entity.setId(Long.valueOf(systemMessageModel.getId()));
            return entity;
        } catch (ParseException e) {
            logger.error("There was an issue parsing the stored CreatedAt date.");
        }
        return null;
    }

}
