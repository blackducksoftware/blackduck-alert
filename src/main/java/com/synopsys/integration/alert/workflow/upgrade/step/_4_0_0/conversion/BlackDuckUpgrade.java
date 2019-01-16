/**
 * blackduck-alert
 *
 * Copyright (C) 2019 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
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
package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.deprecated.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.database.deprecated.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;

@Component
public class BlackDuckUpgrade extends DataUpgrade {
    private final FieldCreatorUtil fieldCreatorUtil;

    @Autowired
    public BlackDuckUpgrade(final GlobalBlackDuckRepository repository, final BaseConfigurationAccessor configurationAccessor, final FieldCreatorUtil fieldCreatorUtil) {
        super(BlackDuckProvider.COMPONENT_NAME, repository, ConfigContextEnum.GLOBAL, configurationAccessor);
        this.fieldCreatorUtil = fieldCreatorUtil;
    }

    @Override
    public List<ConfigurationFieldModel> convertEntityToFieldList(final DatabaseEntity databaseEntity) {
        final GlobalBlackDuckConfigEntity entity = (GlobalBlackDuckConfigEntity) databaseEntity;
        final List<ConfigurationFieldModel> fieldModels = new LinkedList<>();

        final String blackDuckApiKey = entity.getBlackDuckApiKey();
        final Integer blackDuckTimeout = entity.getBlackDuckTimeout();
        final String blackDuckUrl = entity.getBlackDuckUrl();

        fieldCreatorUtil.addFieldModel(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY, blackDuckApiKey, fieldModels);
        fieldCreatorUtil.addFieldModel(BlackDuckDescriptor.KEY_BLACKDUCK_URL, blackDuckUrl, fieldModels);
        fieldCreatorUtil.addFieldModel(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT, blackDuckTimeout, fieldModels);

        return fieldModels;
    }
}
