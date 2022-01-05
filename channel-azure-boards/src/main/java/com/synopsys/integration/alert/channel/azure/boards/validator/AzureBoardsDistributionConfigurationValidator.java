/*
 * channel-azure-boards
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.validator;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.api.channel.CommonChannelDistributionValidator;
import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;

@Component
public class AzureBoardsDistributionConfigurationValidator implements DistributionConfigurationValidator {
    private final CommonChannelDistributionValidator commonChannelDistributionValidator;

    @Autowired
    public AzureBoardsDistributionConfigurationValidator(CommonChannelDistributionValidator commonChannelDistributionValidator) {
        this.commonChannelDistributionValidator = commonChannelDistributionValidator;
    }

    @Override
    public Set<AlertFieldStatus> validate(JobFieldModel jobFieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromJobFieldModel(jobFieldModel);

        commonChannelDistributionValidator.validate(configurationFieldValidator);
        configurationFieldValidator.validateRequiredFieldsAreNotBlank(AzureBoardsDescriptor.KEY_AZURE_PROJECT, AzureBoardsDescriptor.KEY_WORK_ITEM_TYPE);
        configurationFieldValidator.validateRequiredRelatedSet(
            AzureBoardsDescriptor.KEY_WORK_ITEM_REOPEN_STATE, AzureBoardsDescriptor.LABEL_WORK_ITEM_REOPEN_STATE,
            AzureBoardsDescriptor.KEY_WORK_ITEM_COMPLETED_STATE
        );

        return configurationFieldValidator.getValidationResults();
    }

}
