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

import org.apache.commons.lang3.BooleanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.api.common.model.exception.AlertException;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.persistence.accessor.ConfigurationModelConfigurationAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptor;
import com.synopsys.integration.alert.component.authentication.descriptor.AuthenticationDescriptorKey;

public class SAMLContext implements Serializable {
    public static final String PARAM_IGNORE_SAML = "ignoreSAML";

    private static final long serialVersionUID = 4696749244318473215L;

    private final transient Logger logger = LoggerFactory.getLogger(SAMLContext.class);

    private final AuthenticationDescriptorKey descriptorKey;
    private final transient ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor;

    public SAMLContext(AuthenticationDescriptorKey descriptorKey, ConfigurationModelConfigurationAccessor configurationModelConfigurationAccessor) {
        this.descriptorKey = descriptorKey;
        this.configurationModelConfigurationAccessor = configurationModelConfigurationAccessor;
    }

    public ConfigurationModel getCurrentConfiguration() throws AlertException {
        return configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(descriptorKey, ConfigContextEnum.GLOBAL).stream()
                   .findFirst()
                   .orElseThrow(() -> new AlertConfigurationException("Settings configuration missing"));
    }

    public boolean isSAMLEnabled() {
        Optional<ConfigurationModel> samlConfig = configurationModelConfigurationAccessor.getConfigurationsByDescriptorKeyAndContext(descriptorKey, ConfigContextEnum.GLOBAL)
                                                      .stream()
                                                      .findFirst();
        return isSAMLEnabled(samlConfig);
    }

    public boolean isSAMLEnabledForRequest(ServletRequest request) {
        String ignoreSAMLRequestParam = request.getParameter(PARAM_IGNORE_SAML);
        return isSAMLEnabled() && !BooleanUtils.toBoolean(ignoreSAMLRequestParam);
    }

    public void disableSAML() {
        try {
            ConfigurationModel configurationModel = getCurrentConfiguration();
            Map<String, ConfigurationFieldModel> fields = configurationModel.getCopyOfKeyToFieldMap();
            ConfigurationFieldModel enabledField = fields.get(AuthenticationDescriptor.KEY_SAML_ENABLED);
            if (null == enabledField) {
                enabledField = ConfigurationFieldModel.create(AuthenticationDescriptor.KEY_SAML_ENABLED);
                fields.put(AuthenticationDescriptor.KEY_SAML_ENABLED, enabledField);
            }
            enabledField.setFieldValue(String.valueOf(false));
            configurationModelConfigurationAccessor.updateConfiguration(configurationModel.getConfigurationId(), fields.values());
        } catch (AlertException ex) {
            logger.error("Error disabling SAML configuration.");
        }
    }

    public boolean isSAMLEnabled(ConfigurationModel configurationModel) {
        return getFieldValueBoolean(configurationModel, AuthenticationDescriptor.KEY_SAML_ENABLED);
    }

    public String getFieldValueOrEmpty(ConfigurationModel configurationModel, String fieldKey) {
        return configurationModel.getField(fieldKey).flatMap(ConfigurationFieldModel::getFieldValue).orElse("");
    }

    public Boolean getFieldValueBoolean(ConfigurationModel configurationModel, String fieldKey) {
        return configurationModel.getField(fieldKey).flatMap(ConfigurationFieldModel::getFieldValue).map(BooleanUtils::toBoolean).orElse(false);
    }

    private boolean isSAMLEnabled(Optional<ConfigurationModel> configurationModel) {
        if (configurationModel.isPresent()) {
            return isSAMLEnabled(configurationModel.get());
        }

        return false;
    }

}
