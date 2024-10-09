/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.channel.email.validator;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatus;
import com.blackduck.integration.alert.api.common.model.errors.AlertFieldStatusMessages;
import com.blackduck.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalConfigurationValidator {
    public static final String REQUIRED_BECAUSE_AUTH = "Field is required to be set because 'auth' is set to 'true'.";

    public ValidationResponseModel validate(EmailGlobalConfigModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        if (StringUtils.isBlank(model.getName())) {
            statuses.add(AlertFieldStatus.error("name", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
        if (StringUtils.isBlank(model.getSmtpHost())) {
            statuses.add(AlertFieldStatus.error("host", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
        if (StringUtils.isBlank(model.getSmtpFrom())) {
            statuses.add(AlertFieldStatus.error("from", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (model.getSmtpAuth().filter(Boolean.TRUE::equals).isPresent()) {
            if (model.getSmtpUsername().filter(StringUtils::isNotBlank).isEmpty()) {
                statuses.add(AlertFieldStatus.error("user", REQUIRED_BECAUSE_AUTH));
            }
            if (model.getSmtpPassword().filter(StringUtils::isNotBlank).isEmpty() && !BooleanUtils.toBoolean(model.getIsSmtpPasswordSet())) {
                statuses.add(AlertFieldStatus.error("password", REQUIRED_BECAUSE_AUTH));
            }
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }
}
