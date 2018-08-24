package com.synopsys.integration.alert.workflow.filter;

import java.util.function.Predicate;

public class OrFieldFilterBuilder extends BinaryOperatorFieldFilterBuilder {

    public OrFieldFilterBuilder(final JsonFilterBuilder leftFilter, final JsonFilterBuilder rightFilter) {
        super(leftFilter, rightFilter, Predicate::or);
    }
}
