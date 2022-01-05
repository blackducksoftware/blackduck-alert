/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.descriptor;

import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.validator.AzureBoardsDistributionConfigurationValidator;
import com.synopsys.integration.alert.channel.azure.boards.validator.AzureBoardsGlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationFieldModelValidator;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
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
    public static final String LABEL_WORK_ITEM_COMMENT = "Comment on Work Items";
    public static final String LABEL_AZURE_PROJECT = "Azure Project";
    public static final String LABEL_WORK_ITEM_TYPE = "Work Item Type";
    public static final String LABEL_WORK_ITEM_COMPLETED_STATE = "Work Item Completed State";
    public static final String LABEL_WORK_ITEM_REOPEN_STATE = "Work Item Reopen State";

    private final AzureBoardsGlobalConfigurationFieldModelValidator azureBoardsGlobalConfigurationValidator;
    private final AzureBoardsDistributionConfigurationValidator azureBoardsDistributionConfigurationValidator;

    @Autowired
    public AzureBoardsDescriptor(AzureBoardsGlobalConfigurationFieldModelValidator azureBoardsGlobalValidator, AzureBoardsDistributionConfigurationValidator azureBoardsDistributionConfigurationValidator) {
        super(ChannelKeys.AZURE_BOARDS, Set.of(ConfigContextEnum.GLOBAL, ConfigContextEnum.DISTRIBUTION));
        this.azureBoardsGlobalConfigurationValidator = azureBoardsGlobalValidator;
        this.azureBoardsDistributionConfigurationValidator = azureBoardsDistributionConfigurationValidator;
    }

    @Override
    public Optional<GlobalConfigurationFieldModelValidator> getGlobalValidator() {
        return Optional.of(azureBoardsGlobalConfigurationValidator);
    }

    @Override
    public Optional<DistributionConfigurationValidator> getDistributionValidator() {
        return Optional.of(azureBoardsDistributionConfigurationValidator);
    }

}


