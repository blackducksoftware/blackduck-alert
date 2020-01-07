/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class LabelValueSelectOption extends AlertSerializableModel implements Comparable<LabelValueSelectOption> {
    private String label;
    private String value;
    private String icon;

    public LabelValueSelectOption(final String labelAndValue) {
        this(labelAndValue, labelAndValue);
    }

    public LabelValueSelectOption(final String label, final String value) {
        this(label, value, null);
    }

    public LabelValueSelectOption(final String label, final String value, final String icon) {
        this.label = label;
        this.value = value;
        this.icon = icon;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(final String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(final String icon) {
        this.icon = icon;
    }

    @Override
    public int compareTo(final LabelValueSelectOption o) {
        return getLabel().compareTo(o.getLabel());
    }
}
