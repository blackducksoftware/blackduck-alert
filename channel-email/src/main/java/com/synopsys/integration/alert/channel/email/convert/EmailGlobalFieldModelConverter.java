/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.convert;

import com.synopsys.integration.alert.common.action.api.GlobalFieldModelToConcreteConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

public class EmailGlobalFieldModelConverter implements GlobalFieldModelToConcreteConverter<EmailGlobalConfigModel> {

    @Override
    public EmailGlobalConfigModel convert(FieldModel globalFieldModel) {
        return null;
    }
}
