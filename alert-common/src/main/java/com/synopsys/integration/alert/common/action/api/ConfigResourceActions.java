/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action.api;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.MultiFieldModel;

public interface ConfigResourceActions {
    ActionResponse<MultiFieldModel> getAllByContextAndDescriptor(String context, String descriptorName);

    ActionResponse<FieldModel> create(FieldModel resource);

    ActionResponse<MultiFieldModel> getAll();

    ActionResponse<FieldModel> getOne(Long id);

    ActionResponse<FieldModel> update(Long id, FieldModel resource);

    ActionResponse<FieldModel> delete(Long id);

    ValidationActionResponse test(FieldModel resource);

    ValidationActionResponse validate(FieldModel resource);

}
