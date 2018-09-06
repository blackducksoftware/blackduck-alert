package com.synopsys.integration.alert.common.workflow.processor;

import java.util.List;

import com.synopsys.integration.alert.common.enumeration.FormatType;
import com.synopsys.integration.alert.common.model.TopicContent;
import com.synopsys.integration.alert.database.entity.NotificationContent;

public abstract class TopicCollector {

    private final FormatType format;

    public TopicCollector(final FormatType format) {
        this.format = format;
    }

    public FormatType getFormat() {
        return format;
    }

    public abstract void insert(NotificationContent notification);

    public abstract List<TopicContent> collect();
}
