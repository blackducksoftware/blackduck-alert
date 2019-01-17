/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.common.descriptor.config.field;

import java.util.Collection;
import java.util.function.BiFunction;

import com.synopsys.integration.alert.common.enumeration.FieldType;
import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;

public class ReadOnlyConfigField extends ConfigField {
    public static ReadOnlyConfigField createRequired(final String key, final String label) {
        return new ReadOnlyConfigField(key, label, true, false);
    }

    public static ReadOnlyConfigField createRequired(final String key, final String label, final ConfigValidationFunction validationFunction) {
        return new ReadOnlyConfigField(key, label, true, false, validationFunction);
    }

    public static ReadOnlyConfigField createGrouped(final String key, final String label, final String group) {
        return new ReadOnlyConfigField(key, label, false, false, group);
    }

    public static ReadOnlyConfigField createGrouped(final String key, final String label, final String group, final ConfigValidationFunction validationFunction) {
        return new ReadOnlyConfigField(key, label, false, false, group, validationFunction);
    }

    public static ReadOnlyConfigField createSensitiveGrouped(final String key, final String label, final String group) {
        return new ReadOnlyConfigField(key, label, false, true, group);
    }

    public static ReadOnlyConfigField createSensitiveGrouped(final String key, final String label, final String group, final ConfigValidationFunction validationFunction) {
        return new ReadOnlyConfigField(key, label, false, true, group, validationFunction);
    }

    public ReadOnlyConfigField(final String key, final String label, final boolean required, final boolean sensitive, final String subGroup) {
        super(key, label, FieldType.READ_ONLY.getFieldTypeName(), required, sensitive, subGroup);
    }

    public ReadOnlyConfigField(final String key, final String label, final boolean required, final boolean sensitive, final String subGroup, final ConfigValidationFunction validationFunction) {
        super(key, label, FieldType.READ_ONLY.getFieldTypeName(), required, sensitive, subGroup, validationFunction);
    }

    public ReadOnlyConfigField(final String key, final String label, final boolean required, final boolean sensitive) {
        super(key, label, FieldType.READ_ONLY.getFieldTypeName(), required, sensitive);
    }

    public ReadOnlyConfigField(final String key, final String label, final boolean required, final boolean sensitive, final ConfigValidationFunction validationFunction) {
        super(key, label, FieldType.READ_ONLY.getFieldTypeName(), required, sensitive, validationFunction);
    }
}
