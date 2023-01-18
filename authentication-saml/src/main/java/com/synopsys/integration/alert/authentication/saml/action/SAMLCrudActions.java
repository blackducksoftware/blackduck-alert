package com.synopsys.integration.alert.authentication.saml.action;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.validator.SAMLConfigurationValidator;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.rest.api.ConfigurationCrudHelper;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SAMLCrudActions {
    private final ConfigurationCrudHelper configurationCrudHelper;
    private final SAMLConfigAccessor configurationAccessor;
    private final SAMLConfigurationValidator configurationValidator;

    @Autowired
    public SAMLCrudActions(AuthorizationManager authorizationManager, SAMLConfigAccessor configurationAccessor, SAMLConfigurationValidator configurationValidator, AuthenticationDescriptorKey authenticationDescriptorKey) {
        this.configurationCrudHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey);
        this.configurationAccessor = configurationAccessor;
        this.configurationValidator = configurationValidator;
    }

    public ActionResponse<SAMLConfigModel> getOne() {
        return configurationCrudHelper.getOne(
            configurationAccessor::getConfiguration);
    }

    public ActionResponse<SAMLConfigModel> create(SAMLConfigModel resource) {
        return configurationCrudHelper.create(
            () -> configurationValidator.validate(resource),
            configurationAccessor::doesConfigurationExist,
            () -> configurationAccessor.createConfiguration(resource)
        );
    }

    public ActionResponse<SAMLConfigModel> update(SAMLConfigModel requestResource) {
        return configurationCrudHelper.update(
            () -> configurationValidator.validate(requestResource),
            configurationAccessor::doesConfigurationExist,
            () -> configurationAccessor.updateConfiguration(requestResource)
        );
    }

    public ActionResponse<SAMLConfigModel> delete() {
        return configurationCrudHelper.delete(
            configurationAccessor::doesConfigurationExist,
            configurationAccessor::deleteConfiguration
        );
    }
}
