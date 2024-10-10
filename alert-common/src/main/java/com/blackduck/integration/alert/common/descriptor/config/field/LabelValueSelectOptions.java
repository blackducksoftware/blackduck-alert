/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.common.descriptor.config.field;

import java.util.List;

public class LabelValueSelectOptions extends FieldOptions<LabelValueSelectOption> {
    public LabelValueSelectOptions(List<LabelValueSelectOption> options) {
        super(options);
    }
}
