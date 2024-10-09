/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.descriptor.validator;

import java.util.Set;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.common.rest.model.FieldModel;

/**
 * @deprecated Global configuration validators will replace old FieldModel validators as Alert switches to a new concrete REST API. This class will be removed in 8.0.0.
 */
@Deprecated(forRemoval = true)
public interface GlobalConfigurationFieldModelValidator {

    Set<AlertFieldStatus> validate(FieldModel fieldModel);
}
