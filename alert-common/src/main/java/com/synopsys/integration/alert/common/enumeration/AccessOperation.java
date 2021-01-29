/*
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
