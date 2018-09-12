package com.synopsys.integration.alert.common.workflow.processor;

import java.util.List;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.model.TopicContent;

public abstract class TopicFormatter {

    private FormatType formatType;

    public TopicFormatter(FormatType formatType) {
        this.formatType = formatType;
    }

    public FormatType getFormat() {
        return formatType;
    }

    public abstract List<TopicContent> format(List<TopicContent> contentList);
}
