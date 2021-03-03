/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field.errors;

import java.util.List;
import java.util.Map;

import com.synopsys.integration.alert.common.util.DataStructureUtils;

public final class AlertFieldStatusConverter {
    public static Map<String, AlertFieldStatus> convertToMap(List<AlertFieldStatus> statusList) {
        return DataStructureUtils.mapToValues(statusList, AlertFieldStatus::getFieldName);
    }

    public static Map<String, String> convertToStringMap(List<AlertFieldStatus> statusList) {
        return DataStructureUtils.mapToMap(statusList, AlertFieldStatus::getFieldName, AlertFieldStatus::getFieldMessage);
    }

}
