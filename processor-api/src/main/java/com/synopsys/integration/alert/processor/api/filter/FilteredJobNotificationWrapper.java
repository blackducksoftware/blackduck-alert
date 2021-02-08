/*
 * processor-api
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.processor.api.filter;

import java.util.List;
import java.util.UUID;

import com.synopsys.integration.alert.common.enumeration.ProcessingType;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class FilteredJobNotificationWrapper extends AlertSerializableModel {
    private final UUID jobId;
    private final ProcessingType processingType;
    private final String channelName;
    private final List<NotificationContentWrapper> jobNotifications;

    public FilteredJobNotificationWrapper(UUID jobId, ProcessingType processingType, String channelName, List<NotificationContentWrapper> jobNotifications) {
        this.jobId = jobId;
        this.processingType = processingType;
        this.channelName = channelName;
        this.jobNotifications = jobNotifications;
    }

    public UUID getJobId() {
        return jobId;
    }

    public ProcessingType getProcessingType() {
        return processingType;
    }

    public String getChannelName() {
        return channelName;
    }

    public List<NotificationContentWrapper> getJobNotifications() {
        return jobNotifications;
    }

}
