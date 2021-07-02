/*
 * provider-blackduck
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.validator;

import java.util.Set;

import com.synopsys.integration.alert.api.provider.ProviderDescriptor;
import com.synopsys.integration.alert.api.provider.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.validator.ConfigurationFieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.DistributionConfigurationValidator;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;

public class BlackDuckDistributionConfigurationValidator implements DistributionConfigurationValidator {
    /*
            ConfigField policyNotificationTypeFilter = new EndpointTableSelectField(BlackDuckDescriptor.KEY_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER, LABEL_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER,
            DESCRIPTION_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER)
                                                       .applyColumn(TableSelectColumn.visible("name", "Name", true, true))
                                                       .applyPaged(true)
                                                       .applyRequiredRelatedField(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES)
                                                       .applyRequiredRelatedField(ChannelDistributionUIConfig.KEY_PROVIDER_NAME)
                                                       .applyRequiredRelatedField(ProviderDescriptor.KEY_PROVIDER_CONFIG_ID)
                                                       .applyPanel(PANEL_NOTIFICATION_FILTERING);

        ConfigField vulnerabilityNotificationTypeFilter = new EndpointSelectField(BlackDuckDescriptor.KEY_BLACKDUCK_VULNERABILITY_NOTIFICATION_TYPE_FILTER, LABEL_BALCKDUCK_VULNERABILITY_NOTIFICATION_TYPE_FILTER,
            DESCRIPTION_BLACKDUCK_VULNERABILITY_NOTIFICATION_TYPE_FILTER)
                                                              .applyMultiSelect(true)
                                                              .applyClearable(true)
                                                              .applyPanel(PANEL_NOTIFICATION_FILTERING)
                                                              .applyRequiredRelatedField(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES);

        return List.of(policyNotificationTypeFilter, vulnerabilityNotificationTypeFilter);
     */

    @Override
    public Set<AlertFieldStatus> validate(JobFieldModel jobFieldModel) {
        ConfigurationFieldValidator configurationFieldValidator = ConfigurationFieldValidator.fromJobFieldModel(jobFieldModel);

        configurationFieldValidator.validateAllOrNoneSet(
            BlackDuckDescriptor.KEY_BLACKDUCK_POLICY_NOTIFICATION_TYPE_FILTER,
            ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES,
            ChannelDistributionUIConfig.KEY_PROVIDER_NAME,
            ProviderDescriptor.KEY_PROVIDER_CONFIG_ID);

        return configurationFieldValidator.getValidationResults();
    }
}
