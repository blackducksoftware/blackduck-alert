package com.synopsys.integration.alert.util;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;

public class AlertFieldStatusConverter {
    public static final Map<String, AlertFieldStatus> convertToMap(List<AlertFieldStatus> statusList) {
        return statusList
                   .stream()
                   .collect(Collectors.toMap(AlertFieldStatus::getFieldName, Function.identity()));
    }

    public static final Map<String, String> convertToStringMap(List<AlertFieldStatus> statusList) {
        return statusList
                   .stream()
                   .collect(Collectors.toMap(AlertFieldStatus::getFieldName, AlertFieldStatus::getFieldMessage));
    }
}
