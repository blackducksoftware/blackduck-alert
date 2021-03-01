/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class LabelValueSelectOption extends AlertSerializableModel implements Comparable<LabelValueSelectOption> {
    private String label;
    private String value;

    public LabelValueSelectOption(String labelAndValue) {
        this(labelAndValue, labelAndValue);
    }

    public LabelValueSelectOption(String label, String value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    @Override
    public int compareTo(LabelValueSelectOption o) {
        return getLabel().compareTo(o.getLabel());
    }

}
