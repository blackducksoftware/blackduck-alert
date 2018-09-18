/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.common.model;

import java.util.Arrays;

import com.synopsys.integration.util.Stringable;

public final class CategoryKey extends Stringable {
    private final String type;
    private final String key;

    private CategoryKey(final String type, final String key) {
        this.type = type;
        this.key = key;
    }

    public static final CategoryKey from(final String type, final String... keyComponentArray) {
        Arrays.sort(keyComponentArray);
        return new CategoryKey(type, String.join("_", keyComponentArray));
    }

    public String getType() {
        return type;
    }

    public String getKey() {
        return key;
    }
}
