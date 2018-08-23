package com.synopsys.integration.alert.workflow.filter;

import java.util.function.Predicate;

import com.synopsys.integration.alert.database.entity.NotificationContent;

public class OrFieldFilterBuilder extends BinaryOperatorFieldFilterBuilder {

    public OrFieldFilterBuilder(final JsonFilterBuilder leftFilter, final JsonFilterBuilder rightFilter) {
        super(leftFilter, rightFilter);
    }

    @Override
    public Predicate<NotificationContent> buildPredicate() {
        return getLeftFilterBuilder().buildPredicate().or(getRightFilterBuilder().buildPredicate());
    }
}
