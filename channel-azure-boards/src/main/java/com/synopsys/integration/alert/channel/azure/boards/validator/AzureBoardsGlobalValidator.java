/*
 * channel-azure-boards
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.channel.azure.boards.validator;

import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.azure.boards.descriptor.AzureBoardsDescriptor;
import com.synopsys.integration.alert.channel.azure.boards.oauth.OAuthRequestValidator;
import com.synopsys.integration.alert.common.descriptor.config.field.errors.AlertFieldStatus;
import com.synopsys.integration.alert.common.descriptor.validator.FieldValidator;
import com.synopsys.integration.alert.common.descriptor.validator.GlobalValidator;
import com.synopsys.integration.alert.common.rest.model.FieldModel;

@Component
public class AzureBoardsGlobalValidator extends GlobalValidator {
    private final OAuthRequestValidator oAuthRequestValidator;

    @Autowired
    public AzureBoardsGlobalValidator(OAuthRequestValidator oAuthRequestValidator) {
        this.oAuthRequestValidator = oAuthRequestValidator;
    }

    @Override
    protected Set<AlertFieldStatus> validate(FieldModel fieldModel) {
        AlertFieldStatus nameStatus = FieldValidator.validateIsARequiredField(fieldModel, AzureBoardsDescriptor.KEY_ORGANIZATION_NAME);
        AlertFieldStatus clientStatus = FieldValidator.validateIsARequiredField(fieldModel, AzureBoardsDescriptor.KEY_CLIENT_ID);
        AlertFieldStatus clientSecretStatus = FieldValidator.validateIsARequiredField(fieldModel, AzureBoardsDescriptor.KEY_CLIENT_SECRET);

        Set<AlertFieldStatus> statuses = new HashSet<>();
        statuses.add(nameStatus);
        statuses.add(clientStatus);
        statuses.add(clientSecretStatus);

        if (oAuthRequestValidator.hasRequests()) {
            AlertFieldStatus oauthStatus = AlertFieldStatus.error(AzureBoardsDescriptor.KEY_OAUTH, "Authentication in Progress cannot perform current action.");
            statuses.add(oauthStatus);
        }

        return statuses;
    }
}
