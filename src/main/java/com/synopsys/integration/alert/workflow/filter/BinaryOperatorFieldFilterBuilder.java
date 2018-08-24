package com.synopsys.integration.alert.workflow.filter;

import java.util.function.Predicate;

import com.synopsys.integration.alert.database.entity.NotificationContent;

public abstract class BinaryOperatorFieldFilterBuilder implements JsonFilterBuilder {
    private final JsonFilterBuilder leftFilterBuilder;
    private final JsonFilterBuilder rightFilterBuilder;

    public BinaryOperatorFieldFilterBuilder(final JsonFilterBuilder leftFilterBuilder, final JsonFilterBuilder rightFilterBuilder) {
        this.leftFilterBuilder = leftFilterBuilder;
        this.rightFilterBuilder = rightFilterBuilder;
    }

    @Override
    public final Predicate<NotificationContent> buildPredicate() {
        return joiningPredicate(leftFilterBuilder.buildPredicate(), rightFilterBuilder.buildPredicate());
    }

    public abstract Predicate<NotificationContent> joiningPredicate(final Predicate<NotificationContent> leftFilter, final Predicate<NotificationContent> rightFilter);
}
