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
package com.synopsys.integration.alert.workflow.startup;

import com.synopsys.integration.util.Stringable;

public class AlertStartupProperty extends Stringable {
    private final String propertyKey;
    private final String fieldName;
    private final boolean alwaysOverride;

    public AlertStartupProperty(final String propertyKey, final String fieldName) {
        this(propertyKey, fieldName, true);
    }

    public AlertStartupProperty(final String propertyKey, final String fieldName, final boolean alwaysOverride) {
        this.propertyKey = propertyKey;
        this.fieldName = fieldName;
        this.alwaysOverride = alwaysOverride;
    }

    public String getPropertyKey() {
        return propertyKey;
    }

    public String getFieldName() {
        return fieldName;
    }

    public boolean isAlwaysOverride() {
        return alwaysOverride;
    }
}
