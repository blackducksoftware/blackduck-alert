package com.synopsys.integration.alert.common.action.api;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public interface GlobalFieldModelToConcreteSaveActions {

    DescriptorKey getDescriptorKey();

    <T extends AlertSerializableModel> GlobalFieldModelToConcreteConverter<T> getFieldModelConverter();

    void updateConcreteModel(FieldModel fieldModel);

    void createConcreteModel(FieldModel fieldModel);
}
