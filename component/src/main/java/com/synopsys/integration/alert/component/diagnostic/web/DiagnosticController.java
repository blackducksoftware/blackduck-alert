/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.diagnostic.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.component.diagnostic.action.DiagnosticCrudActions;
import com.synopsys.integration.alert.component.diagnostic.model.DiagnosticModel;

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
