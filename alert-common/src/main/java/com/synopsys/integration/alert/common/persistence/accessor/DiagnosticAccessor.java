package com.synopsys.integration.alert.common.persistence.accessor;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public interface DiagnosticAccessor<T extends AlertSerializableModel> {
    T getDiagnosticInfo();
}
