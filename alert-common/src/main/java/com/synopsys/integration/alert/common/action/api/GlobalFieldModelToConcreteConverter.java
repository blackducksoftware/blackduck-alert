package com.synopsys.integration.alert.common.action.api;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

public interface GlobalFieldModelToConcreteConverter {
    <T extends AlertSerializableModel> T convert(FieldModel globalFieldModel);
}
