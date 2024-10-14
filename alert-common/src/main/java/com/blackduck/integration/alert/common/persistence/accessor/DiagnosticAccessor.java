/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.persistence.accessor;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public interface DiagnosticAccessor<T extends AlertSerializableModel> {
    T getDiagnosticInfo();
}
