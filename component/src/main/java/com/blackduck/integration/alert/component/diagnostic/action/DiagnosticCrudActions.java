/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.action;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.diagnostic.database.DefaultDiagnosticAccessor;
import com.blackduck.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.blackduck.integration.alert.component.settings.descriptor.SettingsDescriptorKey;

@Component
public class DiagnosticCrudActions {
    private final ConfigurationCrudHelper configurationHelper;
    private final DefaultDiagnosticAccessor diagnosticAccessor;

    @Autowired
    public DiagnosticCrudActions(AuthorizationManager authorizationManager, SettingsDescriptorKey settingsDescriptorKey, DefaultDiagnosticAccessor diagnosticAccessor) {
        this.configurationHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, settingsDescriptorKey);
        this.diagnosticAccessor = diagnosticAccessor;
    }

    public ActionResponse<DiagnosticModel> getOne() {
        return configurationHelper.getOne(this::getDiagnosticModel);
    }

    private Optional<DiagnosticModel> getDiagnosticModel() {
        return Optional.of(diagnosticAccessor.getDiagnosticInfo());
    }
}
