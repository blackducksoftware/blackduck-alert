/*
 * provider
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.provider.blackduck.collector.util;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.ItemOperation;
import com.synopsys.integration.blackduck.api.manual.enumeration.OperationType;

@Component
public class OperationUtil {
    public ItemOperation getItemOperation(OperationType operationType) {
        switch (operationType) {
            case CREATE:
                return ItemOperation.ADD;
            case DELETE:
                return ItemOperation.DELETE;
            case UPDATE:
                return ItemOperation.UPDATE;
            default:
                return ItemOperation.INFO;
        }
    }

}
