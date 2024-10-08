/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.validator;

import java.util.Set;

import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.rest.model.JobFieldModel;

public interface DistributionConfigurationValidator {

    Set<AlertFieldStatus> validate(JobFieldModel jobFieldModel);
}
