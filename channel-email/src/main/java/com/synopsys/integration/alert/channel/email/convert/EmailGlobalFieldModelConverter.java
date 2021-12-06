package com.synopsys.integration.alert.channel.email.convert;

import java.util.Optional;

import com.synopsys.integration.alert.common.action.api.GlobalFieldModelToConcreteConverter;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

public class EmailGlobalFieldModelConverter implements GlobalFieldModelToConcreteConverter<EmailGlobalConfigModel> {

    @Override
    public Optional<EmailGlobalConfigModel> convert(FieldModel globalFieldModel) {
        return Optional.empty();
    }
}
