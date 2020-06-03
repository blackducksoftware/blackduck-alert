/**
 * alert-database
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.message.model.DateRange;
import com.synopsys.integration.alert.common.persistence.accessor.SystemMessageUtility;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.rest.RestConstants;

@Component
public class DefaultSystemMessageUtility implements SystemMessageUtility {
    private Logger logger = LoggerFactory.getLogger(DefaultSystemMessageUtility.class);
    private final SystemMessageRepository systemMessageRepository;

    @Autowired
    public DefaultSystemMessageUtility(SystemMessageRepository systemMessageRepository) {
        this.systemMessageRepository = systemMessageRepository;
    }

    @Override
    @Transactional
    public void addSystemMessage(String message, SystemMessageSeverity severity, SystemMessageType messageType) {
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        SystemMessageEntity systemMessage = new SystemMessageEntity(currentTime, severity.name(), message, messageType.name());
        systemMessageRepository.save(systemMessage);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeSystemMessagesByType(SystemMessageType messageType) {
        List<SystemMessageEntity> messages = systemMessageRepository.findByType(messageType.name());
        systemMessageRepository.deleteAll(messages);
    }

    @Override
    @Transactional
    public List<SystemMessageModel> getSystemMessages() {
        return convertAllToSystemMessageModel(systemMessageRepository.findAll());
    }

    @Override
    @Transactional
    public List<SystemMessageModel> getSystemMessagesAfter(OffsetDateTime date) {
        OffsetDateTime currentTime = DateUtils.createCurrentDateTimestamp();
        return convertAllToSystemMessageModel(systemMessageRepository.findByCreatedBetween(date, currentTime));
    }

    @Override
    @Transactional
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
    @Transactional
    public List<SystemMessageModel> findBetween(DateRange dateRange) {
        return systemMessageRepository.findByCreatedBetween(dateRange.getStart(), dateRange.getEnd()).stream().map(this::convertToSystemMessageModel).collect(Collectors.toList());
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteSystemMessages(List<SystemMessageModel> messagesToDelete) {
        List<SystemMessageEntity> convertedMessages = messagesToDelete.stream()
                                                          .map(this::convertToSystemMessage)
                                                          .filter(Objects::nonNull)
                                                          .collect(Collectors.toList());
        systemMessageRepository.deleteAll(convertedMessages);
    }

    private List<SystemMessageModel> convertAllToSystemMessageModel(List<SystemMessageEntity> systemMessages) {
        return systemMessages
                   .stream()
                   .map(this::convertToSystemMessageModel)
                   .collect(Collectors.toList());
    }

    private SystemMessageModel convertToSystemMessageModel(SystemMessageEntity systemMessage) {
        String createdAt = DateUtils.formatDate(systemMessage.getCreated(), RestConstants.JSON_DATE_FORMAT);
        return new SystemMessageModel(String.valueOf(systemMessage.getId()), systemMessage.getSeverity(), createdAt, systemMessage.getContent(), systemMessage.getType());
    }

    private SystemMessageEntity convertToSystemMessage(SystemMessageModel systemMessageModel) {
        try {
            OffsetDateTime date = DateUtils.parseDate(systemMessageModel.getCreatedAt(), RestConstants.JSON_DATE_FORMAT);
            SystemMessageEntity entity = new SystemMessageEntity(date, systemMessageModel.getSeverity(), systemMessageModel.getContent(), systemMessageModel.getType());
            entity.setId(Long.valueOf(systemMessageModel.getId()));
            return entity;
        } catch (ParseException e) {
            logger.error("There was an issue parsing the stored CreatedAt date.");
        }
        return null;
    }

}
