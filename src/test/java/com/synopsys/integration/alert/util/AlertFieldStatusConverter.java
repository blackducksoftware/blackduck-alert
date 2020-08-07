package com.synopsys.integration.alert.util;

import java.util.List;
import java.util.Map;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.util.DataStructureUtils;

public final class AlertFieldStatusConverter {
    public static final Map<String, AlertFieldStatus> convertToMap(List<AlertFieldStatus> statusList) {
        return DataStructureUtils.mapToValues(statusList, AlertFieldStatus::getFieldName);
    }

    public static final Map<String, String> convertToStringMap(List<AlertFieldStatus> statusList) {
        return DataStructureUtils.mapToMap(statusList, AlertFieldStatus::getFieldName, AlertFieldStatus::getFieldMessage);
    }
}
