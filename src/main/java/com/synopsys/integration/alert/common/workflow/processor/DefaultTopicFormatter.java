package com.synopsys.integration.alert.common.workflow.processor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.model.TopicContent;

@Component
public class DefaultTopicFormatter extends TopicFormatter {

    @Autowired
    public DefaultTopicFormatter() {
        super(FormatType.DEFAULT);
    }

    @Override
    public List<TopicContent> format(final List<TopicContent> contentList) {
        return contentList;
    }
}
