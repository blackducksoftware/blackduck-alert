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

import com.synopsys.integration.alert.channel.email.web.EmailGlobalConfigModel;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatusMessages;

@Component
public class EmailGlobalConfigurationValidator {
    public static final String REQUIRED_BECAUSE_AUTH = "Field is required to be set because 'auth' is set to 'true'.";

    public Set<AlertFieldStatus> validate(EmailGlobalConfigModel model) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        if (StringUtils.isBlank(model.host)) {
            statuses.add(AlertFieldStatus.error("host", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }
        if (StringUtils.isBlank(model.from)) {
            statuses.add(AlertFieldStatus.error("from", AlertFieldStatusMessages.REQUIRED_FIELD_MISSING));
        }

        if (Boolean.TRUE.equals(model.auth)) {
            if (StringUtils.isBlank(model.user)) {
                statuses.add(AlertFieldStatus.error("user", REQUIRED_BECAUSE_AUTH));
            }
            if (StringUtils.isBlank(model.password)) {
                statuses.add(AlertFieldStatus.error("password", REQUIRED_BECAUSE_AUTH));
            }
        }

        return statuses;
    }
}
