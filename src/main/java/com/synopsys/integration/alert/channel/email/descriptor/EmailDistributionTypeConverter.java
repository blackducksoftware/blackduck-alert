/**
 * blackduck-alert
 *
 * Copyright (C) 2018 Black Duck Software, Inc.
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
package com.synopsys.integration.alert.channel.email.descriptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.channel.email.EmailGroupChannel;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.descriptor.config.CommonTypeConverter;
import com.synopsys.integration.alert.common.descriptor.config.TypeConverter;
import com.synopsys.integration.alert.database.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.entity.repository.CommonDistributionRepository;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;

@Component
public class EmailDistributionTypeConverter extends TypeConverter {
    private final CommonTypeConverter commonTypeConverter;
    private final CommonDistributionRepository commonDistributionRepository;

    @Autowired
    public EmailDistributionTypeConverter(final ContentConverter contentConverter, final CommonTypeConverter commonTypeConverter, final CommonDistributionRepository commonDistributionRepository) {
        super(contentConverter);
        this.commonTypeConverter = commonTypeConverter;
        this.commonDistributionRepository = commonDistributionRepository;
    }

    @Override
    public Config getConfigFromJson(final String json) {
        return getContentConverter().getJsonContent(json, EmailDistributionConfig.class);
    }

    @Override
    public DatabaseEntity populateEntityFromConfig(final Config restModel) {
        final EmailDistributionConfig emailRestModel = (EmailDistributionConfig) restModel;
        final EmailGroupDistributionConfigEntity emailEntity = new EmailGroupDistributionConfigEntity(emailRestModel.getGroupName(), emailRestModel.getEmailTemplateLogoImage(), emailRestModel.getEmailSubjectLine());
        addIdToEntityPK(emailRestModel.getId(), emailEntity);
        return emailEntity;
    }

    @Override
    public Config populateConfigFromEntity(final DatabaseEntity entity) {
        final EmailGroupDistributionConfigEntity emailEntity = (EmailGroupDistributionConfigEntity) entity;
        final EmailDistributionConfig emailRestModel = new EmailDistributionConfig();
        final String id = getContentConverter().getStringValue(emailEntity.getId());
        emailRestModel.setDistributionConfigId(id);
        emailRestModel.setGroupName(emailEntity.getGroupName());
        emailRestModel.setEmailTemplateLogoImage(emailEntity.getEmailTemplateLogoImage());
        emailRestModel.setEmailSubjectLine(emailEntity.getEmailSubjectLine());
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.findByDistributionConfigIdAndDistributionType(emailEntity.getId(), EmailGroupChannel.COMPONENT_NAME);
        return commonTypeConverter.populateCommonFieldsFromEntity(emailRestModel, commonEntity);
    }

}
