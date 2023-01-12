package com.synopsys.integration.alert.authentication.saml.action;

import com.synopsys.integration.alert.authentication.saml.database.accessor.AuthenticationSAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.AuthenticationSAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.validator.AuthenticationSAMLConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationSAMLCrudActions {
    private final ConfigurationCrudHelper configurationCrudHelper;
    private final AuthenticationSAMLConfigAccessor configurationAccessor;
    private final AuthenticationSAMLConfigurationValidator configurationValidator;

    @Autowired
    public AuthenticationSAMLCrudActions(AuthorizationManager authorizationManager, AuthenticationSAMLConfigAccessor configurationAccessor, AuthenticationSAMLConfigurationValidator configurationValidator, AuthenticationDescriptorKey authenticationDescriptorKey) {
        this.configurationCrudHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey);
        this.configurationAccessor = configurationAccessor;
        this.configurationValidator = configurationValidator;
    }

    public ActionResponse<AuthenticationSAMLConfigModel> getOne() {
        return configurationCrudHelper.getOne(
            configurationAccessor::getConfiguration);
    }

    public ActionResponse<AuthenticationSAMLConfigModel> create(AuthenticationSAMLConfigModel resource) {
        return configurationCrudHelper.create(
            () -> configurationValidator.validate(resource),
            configurationAccessor::doesConfigurationExist,
            () -> configurationAccessor.createConfiguration(resource)
        );
    }

    public ActionResponse<AuthenticationSAMLConfigModel> update(AuthenticationSAMLConfigModel requestResource) {
        return configurationCrudHelper.update(
            () -> configurationValidator.validate(requestResource),
            configurationAccessor::doesConfigurationExist,
            () -> configurationAccessor.updateConfiguration(requestResource)
        );
    }

    public ActionResponse<AuthenticationSAMLConfigModel> delete() {
        return configurationCrudHelper.delete(
            configurationAccessor::doesConfigurationExist,
            configurationAccessor::deleteConfiguration
        );
    }
}
