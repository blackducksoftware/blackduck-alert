package com.synopsys.integration.alert.workflow.filter;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import com.synopsys.integration.alert.database.entity.NotificationContent;

public abstract class BinaryOperatorFieldFilterBuilder implements JsonFilterBuilder {
    private final JsonFilterBuilder leftFilterBuilder;
    private final JsonFilterBuilder rightFilterBuilder;
    private final BiFunction<Predicate<NotificationContent>, Predicate<NotificationContent>, Predicate<NotificationContent>> binaryOperator;

    public BinaryOperatorFieldFilterBuilder(final JsonFilterBuilder leftFilterBuilder, final JsonFilterBuilder rightFilterBuilder,
            final BiFunction<Predicate<NotificationContent>, Predicate<NotificationContent>, Predicate<NotificationContent>> binaryOperator) {
        this.leftFilterBuilder = leftFilterBuilder;
        this.rightFilterBuilder = rightFilterBuilder;
        this.binaryOperator = binaryOperator;
    }

    @Override
    public final Predicate<NotificationContent> buildPredicate() {
        return binaryOperator.apply(leftFilterBuilder.buildPredicate(), rightFilterBuilder.buildPredicate());
    }
}
