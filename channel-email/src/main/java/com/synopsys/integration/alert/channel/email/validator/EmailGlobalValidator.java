/*
 * channel-email
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.email.validator;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.FieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalValidator;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;
import com.synopsys.integration.alert.service.email.enumeration.EmailPropertyKeys;

@Component
public class EmailGlobalValidator extends GlobalValidator {
    @Override
    protected Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        AlertFieldStatus hostStatus = FieldValidator.validateIsARequiredField(fieldModel, EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey());
        AlertFieldStatus fromStatus = FieldValidator.validateIsARequiredField(fieldModel, EmailPropertyKeys.JAVAMAIL_FROM_KEY.getPropertyKey());

        AlertFieldStatus portStatus = FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey());
        AlertFieldStatus connectionTimeoutStatus = FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_CONNECTION_TIMEOUT_KEY.getPropertyKey());
        AlertFieldStatus timeoutStatus = FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_TIMEOUT_KEY.getPropertyKey());
        AlertFieldStatus writeTimeoutStatus = FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_WRITETIMEOUT_KEY.getPropertyKey());
        AlertFieldStatus localhostPortStatus = FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_LOCALHOST_PORT_KEY.getPropertyKey());
        AlertFieldStatus authNTLMFlagsStatus = FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_AUTH_NTLM_FLAGS_KEY.getPropertyKey());
        AlertFieldStatus proxyPortStatus = FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_PROXY_PORT_KEY.getPropertyKey());
        AlertFieldStatus socksPortStatus = FieldValidator.validateIsANumber(fieldModel, EmailPropertyKeys.JAVAMAIL_SOCKS_PORT_KEY.getPropertyKey());

        Set<AlertFieldStatus> fieldStatuses = Set.of(hostStatus, fromStatus, portStatus, connectionTimeoutStatus, timeoutStatus, writeTimeoutStatus, localhostPortStatus,
            authNTLMFlagsStatus, proxyPortStatus, socksPortStatus);

        Boolean useAuth = fieldModel.getFieldValueModel(EmailPropertyKeys.JAVAMAIL_AUTH_KEY.getPropertyKey())
                              .flatMap(FieldValueModel::getValue)
                              .map(Boolean::valueOf)
                              .orElse(false);

        List<AlertFieldStatus> authRelatedStatuses = List.of();
        if (useAuth) {
            authRelatedStatuses = FieldValidator.containsRequiredFields(fieldModel, List.of(
                EmailPropertyKeys.JAVAMAIL_USER_KEY.getPropertyKey(),
                EmailPropertyKeys.JAVAMAIL_PASSWORD_KEY.getPropertyKey()
            ));
        }

        return Stream.of(fieldStatuses, authRelatedStatuses).flatMap(Collection::stream).collect(Collectors.toSet());
    }
}
