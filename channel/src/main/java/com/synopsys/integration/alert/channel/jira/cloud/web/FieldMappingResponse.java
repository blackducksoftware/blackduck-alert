package com.synopsys.integration.alert.channel.jira.cloud.web;

import java.util.List;

import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOption;
import com.synopsys.integration.alert.common.descriptor.config.field.LabelValueSelectOptions;

public class FieldMappingResponse {
    private LabelValueSelectOptions leftSideOptions;
    private LabelValueSelectOptions rightSideOptions;

    public FieldMappingResponse(List<LabelValueSelectOption> leftSideOptions, List<LabelValueSelectOption> rightSideOptions) {
        this.leftSideOptions = new LabelValueSelectOptions(leftSideOptions);
        this.rightSideOptions = new LabelValueSelectOptions(rightSideOptions);
    }

    public LabelValueSelectOptions getLeftSideOptions() {
        return leftSideOptions;
    }

    public LabelValueSelectOptions getRightSideOptions() {
        return rightSideOptions;
    }
}
