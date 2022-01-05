/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.descriptor.config.field;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class LabelValueSelectOption extends AlertSerializableModel implements Comparable<LabelValueSelectOption> {
    private final String label;
    private final String value;

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
