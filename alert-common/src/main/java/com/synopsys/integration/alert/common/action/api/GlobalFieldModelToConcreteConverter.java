package com.synopsys.integration.alert.common.action.api;

import java.util.Optional;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

public interface GlobalFieldModelToConcreteConverter<T extends AlertSerializableModel> {
    Optional<T> convert(FieldModel globalFieldModel);
}
