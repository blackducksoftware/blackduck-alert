/**
 * blackduck-alert
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
package com.synopsys.integration.alert.channel.azure.boards.descriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.AzureBoardsChannelKey;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;

@Component
public class AzureBoardsDescriptor extends ChannelDescriptor {
    public static final String KEY_AZURE_PROJECT = "channel.azure.boards.project";
    public static final String KEY_AZURE_BOARD = "channel.azure.boards.board";
    public static final String KEY_WORK_ITEM_TYPE = "channel.azure.boards.work.item.type";
    public static final String KEY_WORK_ITEM_COMPLETED_STATE = "channel.azure.boards.work.item.completed.state";
    public static final String KEY_WORK_ITEM_REOPEN_STATE = "channel.azure.boards.work.item.reopen.state";

    public static final String KEY_AZURE_BOARDS_URL = "azure.boards.url";
    public static final String KEY_ORGANIZATION_NAME = "azure.boards.organization.name";
    public static final String KEY_CONSUMER_KEY = "azure.boards.consumer.key";
    public static final String KEY_PRIVATE_KEY = "azure.boards.private.key";
    public static final String KEY_OAUTH = "azure.boards.oauth";
    public static final String KEY_ACCESS_TOKEN = "azure.boards.access.token";

    public static final String AZURE_BOARDS_LABEL = "Azure Boards";
    public static final String AZURE_BOARDS_URL = "azure_boards";
    public static final String AZURE_BOARDS_DESCRIPTION = "Configure the Azure Boards instance that Alert will send issue updates to.";

    @Autowired
    public AzureBoardsDescriptor(AzureBoardsChannelKey channelKey, AzureBoardsDistributionUIConfig azureBoardsDistributionUIConfig, AzureBoardsGlobalUIConfig azureBoardsGlobalUIConfig) {
        super(channelKey, azureBoardsDistributionUIConfig, azureBoardsGlobalUIConfig);
    }
}


