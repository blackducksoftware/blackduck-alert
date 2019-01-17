package com.synopsys.integration.alert.common.descriptor.config.field;

import java.util.Collection;
import java.util.function.BiFunction;

import com.synopsys.integration.alert.web.model.FieldModel;
import com.synopsys.integration.alert.web.model.FieldValueModel;

public interface ConfigValidationFunction extends BiFunction<FieldValueModel, FieldModel, Collection<String>> {
}
