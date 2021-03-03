/*
 * channel
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
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

@Component
public class AzureBoardsDistributionUIConfig extends ChannelDistributionUIConfig {
    private static final String LABEL_WORK_ITEM_COMMENT = "Comment on Work Items";
    private static final String LABEL_AZURE_PROJECT = "Azure Project";
    private static final String LABEL_WORK_ITEM_TYPE = "Work Item Type";
    private static final String LABEL_WORK_ITEM_COMPLETED_STATE = "Work Item Completed State";
    private static final String LABEL_WORK_ITEM_REOPEN_STATE = "Work Item Reopen State";

    private static final String DESCRIPTION_WORK_ITEM_COMMENT = "If selected, Alert will comment on Work Items it created when updates occur.";
    private static final String DESCRIPTION_AZURE_PROJECT = "The project name or id in Azure Boards.";
    private static final String DESCRIPTION_WORK_ITEM_TYPE = "The work item type in Azure Boards.";
    private static final String DESCRIPTION_WORK_ITEM_COMPLETED_STATE = "The state a work item should result in if Alert receives a DELETE operation for it.";
    private static final String DESCRIPTION_WORK_ITEM_REOPEN_STATE = "The state a work item should result in if Alert receives an ADD operation and the work item is in a completed state.";

    public AzureBoardsDistributionUIConfig() {
        super(ChannelKey.AZURE_BOARDS, AzureBoardsDescriptor.AZURE_BOARDS_LABEL, AzureBoardsDescriptor.AZURE_BOARDS_URL);
    }

    @Override
    public List<ConfigField> createChannelDistributionFields() {
        ConfigField azureWorkItemComments = new CheckboxConfigField(AzureBoardsDescriptor.KEY_WORK_ITEM_COMMENT, LABEL_WORK_ITEM_COMMENT, DESCRIPTION_WORK_ITEM_COMMENT);
        ConfigField azureProject = new TextInputConfigField(AzureBoardsDescriptor.KEY_AZURE_PROJECT, LABEL_AZURE_PROJECT, DESCRIPTION_AZURE_PROJECT).applyRequired(true);
        ConfigField workItemType = new TextInputConfigField(AzureBoardsDescriptor.KEY_WORK_ITEM_TYPE, LABEL_WORK_ITEM_TYPE, DESCRIPTION_WORK_ITEM_TYPE)
                                       .applyRequired(true)
                                       .applyDefaultValue(AzureBoardsDescriptor.DEFAULT_WORK_ITEM_TYPE);
        ConfigField workItemCompletedState = new TextInputConfigField(AzureBoardsDescriptor.KEY_WORK_ITEM_COMPLETED_STATE, LABEL_WORK_ITEM_COMPLETED_STATE, DESCRIPTION_WORK_ITEM_COMPLETED_STATE);
        ConfigField workItemReopenState = new TextInputConfigField(AzureBoardsDescriptor.KEY_WORK_ITEM_REOPEN_STATE, LABEL_WORK_ITEM_REOPEN_STATE, DESCRIPTION_WORK_ITEM_REOPEN_STATE);

        return List.of(azureWorkItemComments, azureProject, workItemType, workItemCompletedState, workItemReopenState);
    }

}
