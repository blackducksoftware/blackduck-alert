package com.synopsys.integration.alert.workflow.filter;

public abstract class BinaryOperatorFieldFilterBuilder implements JsonFilterBuilder {
    private final JsonFilterBuilder leftFilterBuilder;
    private final JsonFilterBuilder rightFilterBuilder;

    public BinaryOperatorFieldFilterBuilder(final JsonFilterBuilder leftFilterBuilder, final JsonFilterBuilder rightFilterBuilder) {
        this.leftFilterBuilder = leftFilterBuilder;
        this.rightFilterBuilder = rightFilterBuilder;
    }

    public JsonFilterBuilder getLeftFilterBuilder() {
        return leftFilterBuilder;
    }

    public JsonFilterBuilder getRightFilterBuilder() {
        return rightFilterBuilder;
    }
}
