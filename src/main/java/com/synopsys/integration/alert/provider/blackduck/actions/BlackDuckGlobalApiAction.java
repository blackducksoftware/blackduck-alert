/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.provider.blackduck.actions;

import java.util.List;

import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ApiAction;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.persistence.accessor.ProviderDataAccessor;
import com.synopsys.integration.alert.common.persistence.model.ConfigurationModel;
import com.synopsys.integration.alert.common.persistence.model.ProviderProject;
import com.synopsys.integration.alert.common.persistence.util.ConfigurationFieldModelConverter;
import com.synopsys.integration.alert.common.provider.lifecycle.ProviderLifecycleManager;
import com.synopsys.integration.alert.common.rest.model.FieldModel;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.factories.BlackDuckPropertiesFactory;

@Component
public class BlackDuckGlobalApiAction extends ApiAction {
    private ProviderLifecycleManager providerLifecycleManager;
    private final ProviderDataAccessor providerDataAccessor;
    private final BlackDuckPropertiesFactory propertiesFactory;
    private final BlackDuckProvider blackDuckProvider;
    private final ConfigurationFieldModelConverter configurationFieldModelConverter;

    public BlackDuckGlobalApiAction(BlackDuckPropertiesFactory propertiesFactory, BlackDuckProvider blackDuckProvider, ProviderLifecycleManager providerLifecycleManager, ProviderDataAccessor providerDataAccessor,
        ConfigurationFieldModelConverter configurationFieldModelConverter) {
        this.blackDuckProvider = blackDuckProvider;
        this.propertiesFactory = propertiesFactory;
        this.providerLifecycleManager = providerLifecycleManager;
        this.providerDataAccessor = providerDataAccessor;
        this.configurationFieldModelConverter = configurationFieldModelConverter;
    }

    @Override
    public FieldModel afterSaveAction(FieldModel fieldModel) throws AlertException {
        handleNewOrUpdatedConfig(fieldModel);
        return super.afterSaveAction(fieldModel);
    }

    @Override
    public FieldModel afterUpdateAction(FieldModel fieldModel) throws AlertException {
        handleNewOrUpdatedConfig(fieldModel);
        return super.afterUpdateAction(fieldModel);
    }

    @Override
    public void afterDeleteAction(String descriptorName, String context) {
        // FIXME this should now be handled by the ProviderLifeCycleManager
        //  providerLifecycleManager.unscheduleTasksForProviderConfig(blackDuckProvider, )

        List<ProviderProject> blackDuckProjects = List.of(); // providerDataAccessor.getProjectsByProviderConfigName(); // FIXME this needs to be updated: providerDataAccessor.findByProviderKey(blackDuckProviderKey);
        providerDataAccessor.deleteProjects(blackDuckProjects);
    }

    private void handleNewOrUpdatedConfig(FieldModel fieldModel) throws AlertDatabaseConstraintException {
        ConfigurationModel configurationModel = configurationFieldModelConverter.convertToConfigurationModel(fieldModel);
        boolean valid = blackDuckProvider.validate(configurationModel);
        if (valid) {
            // FIXME figure out what to do with this: Optional<String> nextRunTime = taskManager.getNextRunTime(BlackDuckAccumulator.TASK_NAME);
            //  if (nextRunTime.isEmpty()) {
            // FIXME this should now be handled by the ProviderLifeCycleManager
            //  providerLifecycleManager.scheduleTasksForProviderConfig(blackDuckProvider, );
            //  }
        }
    }

}
