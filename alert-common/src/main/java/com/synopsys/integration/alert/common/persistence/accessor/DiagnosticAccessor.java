/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public interface DiagnosticAccessor<T extends AlertSerializableModel> {
    T getDiagnosticInfo();
}
