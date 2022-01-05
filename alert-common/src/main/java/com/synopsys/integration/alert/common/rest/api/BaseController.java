/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.rest.api;

import java.util.function.BiFunction;

import org.springframework.web.bind.annotation.RequestMapping;

import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.descriptor.api.model.DescriptorKey;

@RequestMapping(AlertRestConstants.BASE_PATH)
// TODO this class is an improper use of abstraction, it should be removed
public abstract class BaseController {
    protected static final String LOGGER_PATTERN_BREAKING_EXPRESSION = "[\n|\r|\t]";

    public static String createSaferLoggableString(String taintedString) {
        return taintedString.replaceAll(LOGGER_PATTERN_BREAKING_EXPRESSION, "_");
    }

    public boolean hasGlobalPermission(BiFunction<String, String, Boolean> permissionChecker, DescriptorKey descriptorKey) {
        return permissionChecker.apply(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
    }

}
