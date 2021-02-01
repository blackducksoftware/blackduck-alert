/*
 * channel
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
package com.synopsys.integration.alert.channel.azure.boards.descriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class AzureBoardsDescriptor extends ChannelDescriptor {
    public static final String AZURE_BOARDS_PREFIX = "azure.boards.";
    public static final String AZURE_BOARDS_CHANNEL_PREFIX = "channel." + AZURE_BOARDS_PREFIX;

    public static final String KEY_AZURE_PROJECT = AZURE_BOARDS_CHANNEL_PREFIX + "project";
    public static final String KEY_WORK_ITEM_TYPE = AZURE_BOARDS_CHANNEL_PREFIX + "work.item.type";
    public static final String KEY_WORK_ITEM_COMMENT = AZURE_BOARDS_CHANNEL_PREFIX + "work.item.comment";
    public static final String KEY_WORK_ITEM_COMPLETED_STATE = AZURE_BOARDS_CHANNEL_PREFIX + "work.item.completed.state";
    public static final String KEY_WORK_ITEM_REOPEN_STATE = AZURE_BOARDS_CHANNEL_PREFIX + "work.item.reopen.state";

    public static final String KEY_AZURE_BOARDS_URL = AZURE_BOARDS_PREFIX + "url";
    public static final String KEY_ORGANIZATION_NAME = AZURE_BOARDS_PREFIX + "organization.name";
    // Within OAuth this field is called client_id, but in Azure it is referred to as App ID
    public static final String KEY_CLIENT_ID = AZURE_BOARDS_PREFIX + "client.id";
    public static final String KEY_CLIENT_SECRET = AZURE_BOARDS_PREFIX + "client.secret";
    public static final String KEY_OAUTH_USER_EMAIL = AZURE_BOARDS_PREFIX + "oauth.user.email";
    public static final String KEY_OAUTH = AZURE_BOARDS_PREFIX + "oauth";
    public static final String KEY_ACCESS_TOKEN = AZURE_BOARDS_PREFIX + "access.token";
    public static final String KEY_REFRESH_TOKEN = AZURE_BOARDS_PREFIX + "refresh.token";
    public static final String KEY_TOKEN_EXPIRATION_MILLIS = AZURE_BOARDS_PREFIX + "token.expiration.millis";

    public static final String AZURE_BOARDS_LABEL = "Azure Boards";
    public static final String AZURE_BOARDS_URL = "azure_boards";
    public static final String AZURE_BOARDS_DESCRIPTION = "Configure the Azure Boards instance that Alert will send issue updates to.";

    public static final String DEFAULT_WORK_ITEM_TYPE = "Task";

    @Autowired
    public AzureBoardsDescriptor(AzureBoardsDistributionUIConfig azureBoardsDistributionUIConfig, AzureBoardsGlobalUIConfig azureBoardsGlobalUIConfig) {
        super(ChannelKeys.AZURE_BOARDS, azureBoardsDistributionUIConfig, azureBoardsGlobalUIConfig);
    }

}


