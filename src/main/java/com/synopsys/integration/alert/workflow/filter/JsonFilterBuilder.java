package com.synopsys.integration.alert.workflow.filter;

import java.util.function.Predicate;

import com.synopsys.integration.alert.database.entity.NotificationContent;

public interface JsonFilterBuilder {
    Predicate<NotificationContent> buildPredicate();
}
