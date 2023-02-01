/*
 * component
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.component.authentication.security.saml;

import java.io.Serializable;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletRequest;

import com.synopsys.integration.alert.authentication.saml.database.accessor.SAMLConfigAccessor;
import com.synopsys.integration.alert.authentication.saml.model.SAMLConfigModel;
import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.api.authentication.descriptor.AuthenticationDescriptorKey;

public class SAMLContext implements Serializable {
    public static final String PARAM_IGNORE_SAML = "ignoreSAML";

    private static final long serialVersionUID = 4696749244318473215L;

    private final transient Logger logger = LoggerFactory.getLogger(SAMLContext.class);

    private final transient SAMLConfigAccessor samlConfigAccessor;

    public SAMLContext(SAMLConfigAccessor samlConfigAccessor) {
        this.samlConfigAccessor = samlConfigAccessor;
    }

    public SAMLConfigModel getCurrentConfiguration() throws AlertException {
        return samlConfigAccessor.getConfiguration()
                   .orElseThrow(() -> new AlertConfigurationException("Settings configuration missing"));
    }

    public boolean isSAMLEnabled() {
        Optional<SAMLConfigModel> samlConfig = samlConfigAccessor.getConfiguration();
        return isSAMLEnabled(samlConfig);
    }

    public boolean isSAMLEnabledForRequest(ServletRequest request) {
        String ignoreSAMLRequestParam = request.getParameter(PARAM_IGNORE_SAML);
        return isSAMLEnabled() && !BooleanUtils.toBoolean(ignoreSAMLRequestParam);
    }

    private boolean isSAMLEnabled(Optional<SAMLConfigModel> configurationModel) {
        if (configurationModel.isPresent()) {
            return configurationModel.get().getEnabled().orElse(false);
        }

        return false;
    }

}
