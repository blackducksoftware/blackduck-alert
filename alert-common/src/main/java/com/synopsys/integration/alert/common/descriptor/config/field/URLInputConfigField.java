package com.synopsys.integration.alert.common.descriptor.config.field;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

public class URLInputConfigField extends TextInputConfigField {

    public URLInputConfigField(String key, String label, String description) {
        super(key, label, description);
        applyValidationFunctions(this::validateURL);
    }

    private Collection<String> validateURL(FieldValueModel fieldValueModel, FieldModel fieldModel) {
        String url = fieldValueModel.getValue().orElse("");
        if (StringUtils.isNotBlank(url)) {
            try {
                new URL(url);
            } catch (MalformedURLException e) {
                return List.of(e.getMessage());
            }
        }

        return List.of();
    }
}
