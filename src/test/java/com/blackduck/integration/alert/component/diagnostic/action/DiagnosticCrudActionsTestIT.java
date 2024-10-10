/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.persistence.model.PermissionKey;
import com.blackduck.integration.alert.common.persistence.model.PermissionMatrixModel;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.diagnostic.database.DefaultDiagnosticAccessor;
import com.blackduck.integration.alert.component.diagnostic.model.DiagnosticModel;
import com.blackduck.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.blackduck.integration.alert.test.common.AuthenticationTestUtils;
import com.blackduck.integration.alert.util.AlertIntegrationTest;

@AlertIntegrationTest
class DiagnosticCrudActionsTestIT {
    @Autowired
    private DefaultDiagnosticAccessor diagnosticAccessor;

    private final SettingsDescriptorKey descriptorKey = new SettingsDescriptorKey();
    private AuthorizationManager authorizationManager;

    @BeforeEach
    public void init() {
        AuthenticationTestUtils authenticationTestUtils = new AuthenticationTestUtils();
        PermissionKey permissionKey = new PermissionKey(ConfigContextEnum.GLOBAL.name(), descriptorKey.getUniversalKey());
        Map<PermissionKey, Integer> permissions = Map.of(permissionKey, AuthenticationTestUtils.FULL_PERMISSIONS);
        authorizationManager = authenticationTestUtils.createAuthorizationManagerWithCurrentUserSet("admin", "admin", () -> new PermissionMatrixModel(permissions));
    }

    @Test
    void getOneEmptyTest() {
        DiagnosticCrudActions crudActions = new DiagnosticCrudActions(authorizationManager, descriptorKey, diagnosticAccessor);
        ActionResponse<DiagnosticModel> actionResponse = crudActions.getOne();

        assertTrue(actionResponse.isSuccessful());
        assertTrue(actionResponse.hasContent());
        assertEquals(HttpStatus.OK, actionResponse.getHttpStatus());
    }
}
