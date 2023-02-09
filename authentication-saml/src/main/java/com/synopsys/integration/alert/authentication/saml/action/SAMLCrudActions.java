package com.synopsys.integration.alert.authentication.saml.action;

import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;
import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import com.synopsys.integration.alert.authentication.saml.security.SAMLManager;
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
    private final SAMLManager samlManager;

    @Autowired
    public SAMLCrudActions(
        AuthorizationManager authorizationManager,
        SAMLConfigAccessor configurationAccessor,
        SAMLConfigurationValidator configurationValidator,
        AuthenticationDescriptorKey authenticationDescriptorKey,
        SAMLManager samlManager
    ) {
        this.configurationCrudHelper = new ConfigurationCrudHelper(authorizationManager, ConfigContextEnum.GLOBAL, authenticationDescriptorKey);
        this.configurationAccessor = configurationAccessor;
        this.configurationValidator = configurationValidator;
        this.samlManager = samlManager;
    }

    public ActionResponse<SAMLConfigModel> getOne() {
        return configurationCrudHelper.getOne(
            configurationAccessor::getConfiguration);
    }

    public ActionResponse<SAMLConfigModel> create(SAMLConfigModel resource) {
        ActionResponse<SAMLConfigModel> response = configurationCrudHelper.create(
            () -> configurationValidator.validate(resource),
            configurationAccessor::doesConfigurationExist,
            () -> configurationAccessor.createConfiguration(resource)
        );

        if (response.isSuccessful()) {
            samlManager.reconfigureSAML();
        }

        return response;
    }

    public ActionResponse<SAMLConfigModel> update(SAMLConfigModel requestResource) {
        ActionResponse<SAMLConfigModel> response = configurationCrudHelper.update(
            () -> configurationValidator.validate(requestResource),
            configurationAccessor::doesConfigurationExist,
            () -> configurationAccessor.updateConfiguration(requestResource)
        );

        if (response.isSuccessful()) {
            samlManager.reconfigureSAML();
        }

        return response;
    }

    public ActionResponse<SAMLConfigModel> delete() {
        ActionResponse<SAMLConfigModel> response = configurationCrudHelper.delete(
            configurationAccessor::doesConfigurationExist,
            configurationAccessor::deleteConfiguration
        );

        if (response.isSuccessful()) {
            samlManager.reconfigureSAML();
        }

        return response;
    }
}
