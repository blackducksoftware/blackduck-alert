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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.deprecated.channel.hipchat.HipChatDistributionConfigEntity;
import com.synopsys.integration.alert.database.deprecated.channel.hipchat.HipChatDistributionRepository;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Component
public class HipChatDistributionUpgrade extends DataUpgrade {
    private final CommonDistributionFieldCreator commonDistributionFieldCreator;
    private final FieldCreatorUtil fieldCreatorUtil;

    @Autowired
    public HipChatDistributionUpgrade(final HipChatDistributionRepository repository, final BaseConfigurationAccessor configurationAccessor,
        final CommonDistributionFieldCreator commonDistributionFieldCreator, final FieldCreatorUtil fieldCreatorUtil) {
        super(HipChatChannel.COMPONENT_NAME, repository, ConfigContextEnum.DISTRIBUTION, configurationAccessor);
        this.commonDistributionFieldCreator = commonDistributionFieldCreator;
        this.fieldCreatorUtil = fieldCreatorUtil;
    }

    @Override
    protected List<ConfigurationFieldModel> convertEntityToFieldList(final DatabaseEntity databaseEntity) {
        final HipChatDistributionConfigEntity entity = (HipChatDistributionConfigEntity) databaseEntity;
        final List<ConfigurationFieldModel> fieldModels = commonDistributionFieldCreator.createCommonFields(getDescriptorName(), entity.getId());
        final String color = entity.getColor();
        final Boolean notify = entity.getNotify();
        final Integer roomId = entity.getRoomId();

        fieldCreatorUtil.addFieldModel(HipChatDescriptor.KEY_ROOM_ID, roomId, fieldModels);
        fieldCreatorUtil.addFieldModel(HipChatDescriptor.KEY_NOTIFY, notify, fieldModels);
        fieldCreatorUtil.addFieldModel(HipChatDescriptor.KEY_COLOR, color, fieldModels);

        return fieldModels;
    }
}
