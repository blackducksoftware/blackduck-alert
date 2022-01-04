/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.enumeration;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.synopsys.integration.alert.common.util.BitwiseUtil;

public enum AccessOperation {
    CREATE(0),
    DELETE(1),
    READ(2),
    WRITE(3),
    EXECUTE(4),
    UPLOAD_FILE_READ(5),
    UPLOAD_FILE_WRITE(6),
    UPLOAD_FILE_DELETE(7);

    private final int bit;

    // We use an assigned value here instead of ordinal so that we know exactly which item has what bit representation and people have to intentionally change them.
    AccessOperation(int bitPosition) {
        this.bit = BitwiseUtil.shiftBitLeft(bitPosition);
    }

    public int getBit() {
        return bit;
    }

    public boolean isPermitted(int permissions) {
        return BitwiseUtil.containsBits(permissions, bit);
    }

    public static Set<AccessOperation> getAllAccessOperations(int permissions) {
        return Stream.of(AccessOperation.values()).filter(operation -> operation.isPermitted(permissions)).collect(Collectors.toSet());
    }

}
