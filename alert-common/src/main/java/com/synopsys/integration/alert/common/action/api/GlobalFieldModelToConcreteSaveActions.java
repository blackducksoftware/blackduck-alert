package com.synopsys.integration.alert.common.action.api;

import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public interface GlobalFieldModelToConcreteSaveActions {

    DescriptorKey getDescriptorKey();

    void updateConcreteModel(FieldModel fieldModel);

    void createConcreteModel(FieldModel fieldModel);
}
