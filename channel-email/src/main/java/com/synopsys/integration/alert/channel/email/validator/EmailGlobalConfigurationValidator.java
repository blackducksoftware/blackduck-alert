/*
 * channel-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.validator;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldCommonMessageKeys;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.rest.model.ValidationResponseModel;
import com.synopsys.integration.alert.service.email.model.EmailGlobalConfigModel;

@Component
public class EmailGlobalConfigurationValidator {
    public static final String REQUIRED_BECAUSE_AUTH = "REQUIRED_BECAUSE_AUTH_KEY";

    public ValidationResponseModel validate(EmailGlobalConfigModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        if (StringUtils.isBlank(model.getName())) {
            statuses.add(AlertFieldStatus.errorWithMessageKey("name", AlertFieldCommonMessageKeys.REQUIRED_FIELD_MISSING_KEY.name()));
        }
        if (model.getSmtpHost().filter(StringUtils::isNotBlank).isEmpty()) {
            statuses.add(AlertFieldStatus.errorWithMessageKey("host", AlertFieldCommonMessageKeys.REQUIRED_FIELD_MISSING_KEY.name()));
        }
        if (model.getSmtpFrom().filter(StringUtils::isNotBlank).isEmpty()) {
            statuses.add(AlertFieldStatus.errorWithMessageKey("from", AlertFieldCommonMessageKeys.REQUIRED_FIELD_MISSING_KEY.name()));
        }

        if (model.getSmtpAuth().filter(Boolean.TRUE::equals).isPresent()) {
            if (model.getSmtpUsername().filter(StringUtils::isNotBlank).isEmpty()) {
                statuses.add(AlertFieldStatus.errorWithMessageKey("user", REQUIRED_BECAUSE_AUTH));
            }
            if (model.getSmtpPassword().filter(StringUtils::isNotBlank).isEmpty()) {
                statuses.add(AlertFieldStatus.errorWithMessageKey("password", REQUIRED_BECAUSE_AUTH));
            }
        }

        if (!statuses.isEmpty()) {
            return ValidationResponseModel.fromStatusCollection(statuses);
        }

        return ValidationResponseModel.success();
    }
}
