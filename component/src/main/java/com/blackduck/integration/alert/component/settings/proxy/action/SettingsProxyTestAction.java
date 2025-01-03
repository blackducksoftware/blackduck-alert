/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.component.settings.proxy.action;

import java.util.function.Supplier;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackduck.integration.alert.api.common.model.ValidationResponseModel;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.action.ValidationActionResponse;
import com.blackduck.integration.alert.common.enumeration.ConfigContextEnum;
import com.blackduck.integration.alert.common.message.model.ConfigurationTestResult;
import com.blackduck.integration.alert.common.rest.api.ConfigurationTestHelper;
import com.blackduck.integration.alert.common.rest.api.ConfigurationValidationHelper;
import com.blackduck.integration.alert.common.rest.model.SettingsProxyModel;
import com.blackduck.integration.alert.common.rest.proxy.ProxyTestService;
import com.blackduck.integration.alert.common.security.authorization.AuthorizationManager;
import com.blackduck.integration.alert.component.settings.descriptor.SettingsDescriptorKey;
import com.blackduck.integration.alert.component.settings.proxy.database.accessor.SettingsProxyConfigAccessor;
import com.blackduck.integration.alert.component.settings.proxy.validator.SettingsProxyValidator;

@Component
public class SettingsProxyTestAction {
    private final ConfigurationValidationHelper validationHelper;
    private final ConfigurationTestHelper testHelper;
    private final SettingsProxyValidator validator;
    private final ProxyTestService proxyTestService;
    private final SettingsProxyConfigAccessor configurationAccessor;

    @Autowired
    public SettingsProxyTestAction(AuthorizationManager authorizationManager, SettingsProxyValidator validator, SettingsDescriptorKey settingsDescriptorKey, ProxyTestService proxyTestService,
        SettingsProxyConfigAccessor configurationAccessor) {
        this.validationHelper = new ConfigurationValidationHelper(authorizationManager, ConfigContextEnum.GLOBAL, settingsDescriptorKey);
        this.testHelper = new ConfigurationTestHelper(authorizationManager, ConfigContextEnum.GLOBAL, settingsDescriptorKey);
        this.validator = validator;
        this.proxyTestService = proxyTestService;
        this.configurationAccessor = configurationAccessor;
    }

    public ActionResponse<ValidationResponseModel> testWithPermissionCheck(String testUrl, SettingsProxyModel requestResource) {
        Supplier<ValidationActionResponse> validationSupplier = () -> validationHelper.validate(() -> validator.validate(requestResource));
        return testHelper.test(validationSupplier, () -> testConfigModelContent(testUrl, requestResource));
    }

    public ConfigurationTestResult testConfigModelContent(String testUrl, SettingsProxyModel settingsProxyModel) {
        if (StringUtils.isBlank(testUrl)) {
            return ConfigurationTestResult.failure("Could not determine what URL to test the proxy. Target URL was not provided or was blank. Please provide a valid URL to test the configuration.");
        }

        if (BooleanUtils.toBoolean(settingsProxyModel.getIsProxyPasswordSet()) && settingsProxyModel.getProxyPassword().isEmpty()) {
            configurationAccessor.getConfiguration()
                .flatMap(SettingsProxyModel::getProxyPassword)
                .ifPresent(settingsProxyModel::setProxyPassword);
        }

        return proxyTestService.pingHost(testUrl, settingsProxyModel);
    }
}
