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

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.enumeration.SystemMessageSeverity;
import com.synopsys.integration.alert.common.enumeration.SystemMessageType;
import com.synopsys.integration.alert.common.message.model.DateRange;

@Component
public class SystemMessageUtility {
    private final SystemMessageRepository systemMessageRepository;

    @Autowired
    public SystemMessageUtility(final SystemMessageRepository systemMessageRepository) {
        this.systemMessageRepository = systemMessageRepository;
    }

    @Transactional
    public void addSystemMessage(final String message, final SystemMessageSeverity severity, final SystemMessageType messageType) {
        final Date currentTime = DateRange.createCurrentDateTimestamp();
        final SystemMessage systemMessage = new SystemMessage(currentTime, severity.name(), message, messageType.name());
        systemMessageRepository.save(systemMessage);
    }

    @Transactional
    public void removeSystemMessagesByType(final SystemMessageType messageType) {
        final List<SystemMessage> messages = systemMessageRepository.findByType(messageType.name());
        systemMessageRepository.deleteAll(messages);
    }

    @Transactional
    public List<SystemMessage> getSystemMessages() {
        return systemMessageRepository.findAll();
    }

    @Transactional
    public List<SystemMessage> getSystemMessagesAfter(final Date date) {
        final Date currentTime = DateRange.createCurrentDateTimestamp();
        return systemMessageRepository.findByCreatedBetween(date, currentTime);
    }

    @Transactional
    public List<SystemMessage> getSystemMessagesBefore(final Date date) {
        final long recordCount = systemMessageRepository.count();
        if (recordCount == 0) {
            return Collections.emptyList();
        } else {
            final SystemMessage oldestMessage = systemMessageRepository.findTopByOrderByCreatedAsc();
            return systemMessageRepository.findByCreatedBetween(oldestMessage.getCreated(), date);
        }
    }

    @Transactional
    public List<SystemMessage> findBetween(final DateRange dateRange) {
        return systemMessageRepository.findByCreatedBetween(dateRange.getStart(), dateRange.getEnd());
    }

    @Transactional
    public void deleteSystemMessages(final List<SystemMessage> messagesToDelete) {
        systemMessageRepository.deleteAll(messagesToDelete);
    }

}
