package com.synopsys.integration.alert.workflow.filter;

import java.util.function.Predicate;

public class AndFieldFilterBuilder extends BinaryOperatorFieldFilterBuilder {

    public AndFieldFilterBuilder(final JsonFilterBuilder leftFilter, final JsonFilterBuilder rightFilter) {
        super(leftFilter, rightFilter, Predicate::and);
    }
}
