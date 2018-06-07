/**
 * hub-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.blackducksoftware.integration.hub.alert.event;

import java.util.Optional;
import java.util.UUID;

import com.blackducksoftware.integration.hub.alert.exception.AlertException;

public class AlertEvent {

    private final String eventId;
    private final String destination;
    private final Object content;

    public AlertEvent(final String destination, final Object content) {
        this.eventId = UUID.randomUUID().toString();
        this.destination = destination;
        this.content = content;
    }

    public String getEventId() {
        return eventId;
    }

    public String getDestination() {
        return destination;
    }

    public <C> Optional<C> getContent(Class<C> contentClass) throws AlertException {
        if (contentClass != null && content != null) {
            if (contentClass.isAssignableFrom(content.getClass())) {
                return Optional.of(contentClass.cast(content));
            } else {
                final String exceptionMessage = String.format("Cannot get content of type %s from %s", contentClass.getName(), content.getClass().getName());
                throw new AlertException(exceptionMessage);
            }
        }
        return Optional.empty();
    }
}
