/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.validator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.FieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalConfigurationValidator;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;

@Component
public class EmailGlobalConfigurationValidator implements GlobalConfigurationValidator {
    @Override
    public Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        Set<AlertFieldStatus> statuses = new HashSet<>();
        FieldValidator.validateIsARequiredField(fieldModel, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey()).ifPresent(statuses::add);
        FieldValidator.validateIsARequiredField(fieldModel, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey()).ifPresent(statuses::add);

        FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()).ifPresent(statuses::add);
        FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey()).ifPresent(statuses::add);
        FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey()).ifPresent(statuses::add);
        FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey()).ifPresent(statuses::add);
        FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey()).ifPresent(statuses::add);
        FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey()).ifPresent(statuses::add);
        FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey()).ifPresent(statuses::add);
        FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey()).ifPresent(statuses::add);

        Boolean useAuth = fieldModel.getFieldValueModel(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey())
                              .flatMap(FieldValueModel::getValue)
                              .map(Boolean::valueOf)
                              .orElse(false);

        if (useAuth) {
            List<AlertFieldStatus> authRelatedStatuses = FieldValidator.containsRequiredFields(fieldModel, List.of(
                EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(),
                EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey()
            ));
            statuses.addAll(authRelatedStatuses);
        }

        return statuses;
    }
}
