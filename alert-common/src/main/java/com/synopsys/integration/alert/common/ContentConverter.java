/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Component;

@Component
public class ContentConverter {
    private final ConversionService conversionService;

    @Autowired
    public ContentConverter(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    public String getStringValue(Object content) {
        return getValue(content, String.class);
    }

    private <C> C getValue(Object value, Class<C> clazz) {
        Optional<C> content = getContent(value, clazz);
        return content.orElse(null);
    }

    private <C> Optional<C> getContent(Object content, Class<C> contentClass) {
        if (contentClass != null && content != null && conversionService.canConvert(content.getClass(), contentClass)) {
            return Optional.ofNullable(conversionService.convert(content, contentClass));
        }
        return Optional.empty();
    }

}
