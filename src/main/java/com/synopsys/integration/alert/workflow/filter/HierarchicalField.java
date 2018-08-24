/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
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
