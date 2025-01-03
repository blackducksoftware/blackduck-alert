/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.diagnostic.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.component.diagnostic.action.DiagnosticCrudActions;
import com.blackduck.integration.alert.component.diagnostic.model.DiagnosticModel;

@RestController
@RequestMapping(AlertRestConstants.DIAGNOSTIC_PATH)
public class DiagnosticController {
    private final DiagnosticCrudActions diagnosticCrudActions;

    @Autowired
    public DiagnosticController(DiagnosticCrudActions diagnosticCrudActions) {
        this.diagnosticCrudActions = diagnosticCrudActions;
    }

    @GetMapping
    public DiagnosticModel getOne() {
        return ResponseFactory.createContentResponseFromAction(diagnosticCrudActions.getOne());
    }
}
