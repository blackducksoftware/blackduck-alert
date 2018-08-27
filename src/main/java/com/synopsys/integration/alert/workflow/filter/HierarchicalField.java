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
package com.synopsys.integration.alert.workflow.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class HierarchicalField {
    private final Collection<String> fieldList;

    private HierarchicalField(final Collection<String> fieldList) {
        this.fieldList = fieldList;
    }

    public static final HierarchicalField topLevelField(final String fieldName) {
        return new HierarchicalField(Arrays.asList(fieldName));
    }

    public static final HierarchicalField nestedField(final String... fieldNames) {
        return new HierarchicalField(Arrays.asList(fieldNames));
    }

    public static final HierarchicalField nestedField(final Collection<String> fieldNames) {
        return new HierarchicalField(fieldNames);
    }

    /**
     * @return an unmodifiable list of fields representing the path to a field nested within an object
     */
    public List<String> getFieldNames() {
        final List<String> list = new ArrayList<>();
        list.addAll(fieldList);
        return Collections.unmodifiableList(list);
    }
}
