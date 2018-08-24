package com.synopsys.integration.alert.workflow.filter;

import java.util.function.Predicate;

import com.synopsys.integration.alert.database.entity.NotificationContent;

public class AndFieldFilterBuilder extends BinaryOperatorFieldFilterBuilder {

    public AndFieldFilterBuilder(final JsonFilterBuilder leftFilter, final JsonFilterBuilder rightFilter) {
        super(leftFilter, rightFilter);
    }

    @Override
    public Predicate<NotificationContent> joiningPredicate(final Predicate<NotificationContent> left, final Predicate<NotificationContent> right) {
        return left.and(right);
    }
}
