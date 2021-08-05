/*
 * channel-azure-boards
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.descriptor;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.CheckboxConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.ConfigField;
import com.synopsys.integration.alert.common.descriptor.config.field.TextInputConfigField;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKeys;

@Component
public class AzureBoardsDistributionUIConfig extends ChannelDistributionUIConfig {
    public AzureBoardsDistributionUIConfig() {
        super(ChannelKeys.AZURE_BOARDS, AzureBoardsDescriptor.AZURE_BOARDS_LABEL, AzureBoardsDescriptor.AZURE_BOARDS_URL);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        ConfigField azureWorkItemComments = new CheckboxConfigField(AzureBoardsDescriptor.KEY_WORK_ITEM_COMMENT, AzureBoardsDescriptor.LABEL_WORK_ITEM_COMMENT, AzureBoardsDescriptor.DESCRIPTION_WORK_ITEM_COMMENT);
        ConfigField azureProject = new TextInputConfigField(AzureBoardsDescriptor.KEY_AZURE_PROJECT, AzureBoardsDescriptor.LABEL_AZURE_PROJECT, AzureBoardsDescriptor.DESCRIPTION_AZURE_PROJECT).applyRequired(true);
        ConfigField workItemType = new TextInputConfigField(AzureBoardsDescriptor.KEY_WORK_ITEM_TYPE, AzureBoardsDescriptor.LABEL_WORK_ITEM_TYPE, AzureBoardsDescriptor.DESCRIPTION_WORK_ITEM_TYPE)
            .applyRequired(true)
            .applyDefaultValue(AzureBoardsDescriptor.DEFAULT_WORK_ITEM_TYPE);
        ConfigField workItemCompletedState = new TextInputConfigField(AzureBoardsDescriptor.KEY_WORK_ITEM_COMPLETED_STATE, AzureBoardsDescriptor.LABEL_WORK_ITEM_COMPLETED_STATE, AzureBoardsDescriptor.DESCRIPTION_WORK_ITEM_COMPLETED_STATE);
        ConfigField workItemReopenState = new TextInputConfigField(AzureBoardsDescriptor.KEY_WORK_ITEM_REOPEN_STATE, AzureBoardsDescriptor.LABEL_WORK_ITEM_REOPEN_STATE, AzureBoardsDescriptor.DESCRIPTION_WORK_ITEM_REOPEN_STATE);

        return List.of(azureWorkItemComments, azureProject, workItemType, workItemCompletedState, workItemReopenState);
    }

}
