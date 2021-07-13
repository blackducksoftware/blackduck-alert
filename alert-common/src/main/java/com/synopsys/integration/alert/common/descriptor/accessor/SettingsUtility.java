/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.accessor;

import java.util.Optional;

import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public interface SettingsUtility {

    DescriptorKey getKey();

    boolean doesConfigurationExist();

    Optional<ConfigurationModel> getConfiguration();

    Optional<FieldModel> getFieldModel() throws AlertException;

    FieldModel saveSettings(FieldModel fieldModel) throws AlertException;

    FieldModel updateSettings(Long id, FieldModel fieldModel) throws AlertException;

}
