/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.validator;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatusMessages;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalConfigurationValidator {
    public static final String REQUIRED_BECAUSE_AUTH = "Field is required to be set because 'auth' is set to 'true'.";

    public ValidationResponseModel validate(EmailGlobalConfigModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        if (model.getHost().filter(StringUtils::isNotBlank).isEmpty()) {
            statuses.add(AlertFieldStatus.error("host", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
        if (model.getFrom().filter(StringUtils::isNotBlank).isEmpty()) {
            statuses.add(AlertFieldStatus.error("from", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (model.getAuth().filter(Boolean.TRUE::equals).isPresent()) {
            if (model.getUsername().filter(StringUtils::isNotBlank).isEmpty()) {
                statuses.add(AlertFieldStatus.error("user", REQUIRED_BECAUSE_AUTH));
            }
            if (model.getPassword().filter(StringUtils::isNotBlank).isEmpty()) {
                statuses.add(AlertFieldStatus.error("password", REQUIRED_BECAUSE_AUTH));
            }
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }
}
