/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.descriptor.validator;

import java.util.Set;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.common.rest.model.JobFieldModel;

public interface DistributionConfigurationValidator {

    Set<AlertFieldStatus> validate(JobFieldModel jobFieldModel);
}
