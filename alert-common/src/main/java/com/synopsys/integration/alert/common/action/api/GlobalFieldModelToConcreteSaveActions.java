/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action.api;

import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

public interface GlobalFieldModelToConcreteSaveActions {

    DescriptorKey getDescriptorKey();

    void updateConcreteModel(FieldModel fieldModel);

    void createConcreteModel(FieldModel fieldModel);

    void deleteConcreteModel(FieldModel fieldModel);
}
