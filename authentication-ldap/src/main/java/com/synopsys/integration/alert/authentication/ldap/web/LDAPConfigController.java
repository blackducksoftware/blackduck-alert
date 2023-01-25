package com.synopsys.integration.alert.authentication.ldap.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.synopsys.integration.alert.api.common.model.ValidationResponseModel;
import com.synopsys.integration.alert.authentication.ldap.action.LDAPCrudActions;
import com.synopsys.integration.alert.authentication.ldap.action.LDAPTestAction;
import com.synopsys.integration.alert.authentication.ldap.action.LDAPValidationAction;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigModel;
import com.synopsys.integration.alert.authentication.ldap.model.LDAPConfigTestModel;
import com.synopsys.integration.alert.common.rest.AlertRestConstants;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.common.rest.api.StaticUniqueConfigResourceController;
import com.synopsys.integration.alert.common.rest.api.ValidateController;

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
        ResponseFactory.createContentResponseFromAction(ldapCrudActions.delete());
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
