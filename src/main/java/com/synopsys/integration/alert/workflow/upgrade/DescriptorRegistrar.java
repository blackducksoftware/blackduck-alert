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
package com.synopsys.integration.alert.workflow.upgrade;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.synopsys.integration.alert.common.database.BaseDescriptorAccessor;
import com.synopsys.integration.alert.common.descriptor.Descriptor;
import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.common.descriptor.config.ui.ProviderDistributionUIConfig;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.enumeration.DescriptorType;
import com.synopsys.integration.alert.common.exception.AlertDatabaseConstraintException;
import com.synopsys.integration.alert.database.api.configuration.model.DefinedFieldModel;
import com.synopsys.integration.alert.database.api.configuration.model.RegisteredDescriptorModel;

@Component
public class DescriptorRegistrar {
    private final Logger logger = LoggerFactory.getLogger(DescriptorRegistrar.class);
    private final List<Descriptor> allDescriptors;
    private final BaseDescriptorAccessor descriptorAccessor;

    @Autowired
    public DescriptorRegistrar(final BaseDescriptorAccessor descriptorAccessor, final List<Descriptor> allDescriptors) {
        this.descriptorAccessor = descriptorAccessor;
        this.allDescriptors = allDescriptors;
    }

    @Transactional
    public void registerDescriptors() throws AlertDatabaseConstraintException {
        for (final Descriptor descriptor : allDescriptors) {
            final String descriptorName = descriptor.getName();
            logger.info("Adding descriptor '{}'", descriptorName);
            final DescriptorType descriptorType = descriptor.getType();

            final RegisteredDescriptorModel registeredDescriptorModel = descriptorAccessor.registerDescriptorWithoutFields(descriptorName, descriptorType);
            final Long descriptorId = registeredDescriptorModel.getId();

            final Collection<DefinedFieldModel> allDescriptorFieldModels = getAllDescriptorFieldModels(descriptor);

            for (final DefinedFieldModel fieldModel : allDescriptorFieldModels) {
                descriptorAccessor.addDescriptorField(descriptorId, fieldModel);
            }
        }
    }

    private Collection<DefinedFieldModel> getAllDescriptorFieldModels(final Descriptor descriptor) {
        final List<DefinedFieldModel> completeDistributionFieldModels = new LinkedList<>();
        final Set<DefinedFieldModel> distributionFieldModels = descriptor.getAllDefinedFields(ConfigContextEnum.DISTRIBUTION);
        completeDistributionFieldModels.addAll(distributionFieldModels);
        completeDistributionFieldModels.addAll(getAllCommonFields());

        final Collection<DefinedFieldModel> allFieldModels = new LinkedList<>();
        final Set<DefinedFieldModel> globalFieldModels = descriptor.getAllDefinedFields(ConfigContextEnum.GLOBAL);
        allFieldModels.addAll(globalFieldModels);
        allFieldModels.addAll(completeDistributionFieldModels);

        return allFieldModels;
    }

    private Collection<DefinedFieldModel> getAllCommonFields() {
        // TODO find a better way to access these fields
        final DefinedFieldModel name = DefinedFieldModel.createDistributionField(ChannelDistributionUIConfig.KEY_NAME);
        final DefinedFieldModel frequency = DefinedFieldModel.createDistributionField(ChannelDistributionUIConfig.KEY_FREQUENCY);
        final DefinedFieldModel channelName = DefinedFieldModel.createDistributionField(ChannelDistributionUIConfig.KEY_CHANNEL_NAME);
        final DefinedFieldModel providerName = DefinedFieldModel.createDistributionField(ChannelDistributionUIConfig.KEY_PROVIDER_NAME);

        final DefinedFieldModel formatType = DefinedFieldModel.createDistributionField(ProviderDistributionUIConfig.KEY_FORMAT_TYPE);
        final DefinedFieldModel notificationTypes = DefinedFieldModel.createDistributionField(ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES);

        return List.of(name, frequency, channelName, providerName, formatType, notificationTypes);
    }
}
