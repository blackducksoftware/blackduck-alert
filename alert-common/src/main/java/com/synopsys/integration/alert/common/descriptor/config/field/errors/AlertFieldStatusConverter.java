/**
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
