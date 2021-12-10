/*
 * alert-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.action.api;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;
import com.synopsys.integration.alert.common.rest.model.ConfigWithMetadata;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

public interface GlobalFieldModelToConcreteConverter<E extends ConfigWithMetadata> {
    <T extends AlertSerializableModel> T convert(FieldModel globalFieldModel);
}
