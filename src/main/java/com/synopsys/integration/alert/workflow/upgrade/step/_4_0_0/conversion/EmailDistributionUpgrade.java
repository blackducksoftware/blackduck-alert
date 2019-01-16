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

import com.synopsys.integration.alert.channel.email.EmailChannel;
import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.common.database.BaseConfigurationAccessor;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.deprecated.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.deprecated.channel.email.EmailGroupDistributionRepository;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;

@Component
public class EmailDistributionUpgrade extends DataUpgrade {
    private final CommonDistributionFieldCreator commonDistributionFieldCreator;
    private final FieldCreatorUtil fieldCreatorUtil;

    @Autowired
    public EmailDistributionUpgrade(final EmailGroupDistributionRepository repository, final BaseConfigurationAccessor configurationAccessor, final CommonDistributionFieldCreator commonDistributionFieldCreator,
        final FieldCreatorUtil fieldCreatorUtil) {
        super(EmailChannel.COMPONENT_NAME, repository, ConfigContextEnum.DISTRIBUTION, configurationAccessor);
        this.commonDistributionFieldCreator = commonDistributionFieldCreator;
        this.fieldCreatorUtil = fieldCreatorUtil;
    }

    @Override
    public List<ConfigurationFieldModel> convertEntityToFieldList(final DatabaseEntity databaseEntity) {
        final EmailGroupDistributionConfigEntity entity = (EmailGroupDistributionConfigEntity) databaseEntity;
        final List<ConfigurationFieldModel> commonFields = commonDistributionFieldCreator.createCommonFields(getDescriptorName(), entity.getId());

        final String emailSubjectLine = entity.getEmailSubjectLine();

        // FIXME This item is specific to Emails and ONLY descriptors that have project owners. Will need to store this in the BD distribution configuration
        //final Boolean projectOwnerOnly = entity.getProjectOwnerOnly();

        fieldCreatorUtil.addFieldModel(EmailDescriptor.KEY_SUBJECT_LINE, emailSubjectLine, commonFields);

        return commonFields;
    }
}
