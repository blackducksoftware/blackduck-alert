/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.authentication.ldap.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.authentication.ldap.action.LDAPCrudActions;
import com.blackduck.integration.alert.authentication.ldap.action.LDAPTestAction;
import com.blackduck.integration.alert.authentication.ldap.action.LDAPValidationAction;
import com.blackduck.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.blackduck.integration.alert.authentication.ldap.model.LDAPConfigTestModel;
import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.common.rest.ResponseFactory;
import com.blackduck.integration.alert.common.rest.api.StaticUniqueConfigResourceController;
import com.blackduck.integration.alert.common.rest.api.ValidateController;

@RestController
@RequestMapping(AlertRestConstants.LDAP_PATH)
public class LDAPConfigController implements StaticUniqueConfigResourceController<LDAPConfigModel>, ValidateController<LDAPConfigModel> {
    private final LDAPCrudActions ldapCrudActions;
    private final LDAPValidationAction ldapValidationAction;
    private final LDAPTestAction ldapTestAction;

    @Autowired
    public LDAPConfigController(LDAPCrudActions ldapCrudActions, LDAPValidationAction ldapValidationAction, LDAPTestAction ldapTestAction) {
        this.ldapCrudActions = ldapCrudActions;
        this.ldapValidationAction = ldapValidationAction;
        this.ldapTestAction = ldapTestAction;
    }

    @Override
    public LDAPConfigModel create(LDAPConfigModel ldapConfigModel) {
        return ResponseFactory.createContentResponseFromAction(ldapCrudActions.create(ldapConfigModel));
    }

    @Override
    public LDAPConfigModel getOne() {
        return ResponseFactory.createContentResponseFromAction(ldapCrudActions.getOne());
    }

    @Override
    public void update(LDAPConfigModel ldapConfigModel) {
        ResponseFactory.createContentResponseFromAction(ldapCrudActions.update(ldapConfigModel));
    }

    @Override
    public void delete() {
        ResponseFactory.createResponseFromAction(ldapCrudActions.delete());
    }

    @Override
    public ValidationResponseModel validate(LDAPConfigModel ldapConfigModel) {
        return ResponseFactory.createContentResponseFromAction(ldapValidationAction.validate(ldapConfigModel));
    }

    @PostMapping("/test")
    public ValidationResponseModel test(@RequestBody LDAPConfigTestModel ldapConfigTestModel) {
        return ResponseFactory.createContentResponseFromAction(ldapTestAction.testAuthentication(ldapConfigTestModel));
    }
}
