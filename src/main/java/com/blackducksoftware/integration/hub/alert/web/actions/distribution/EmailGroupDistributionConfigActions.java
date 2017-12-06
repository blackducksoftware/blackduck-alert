/**
 * Copyright (C) 2017 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
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
package com.blackducksoftware.integration.hub.alert.web.actions.distribution;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.CommonDistributionRepository;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.EmailGroupDistributionRestModel;

@Component
public class EmailGroupDistributionConfigActions extends DistributionConfigActions<EmailGroupDistributionConfigEntity, EmailGroupDistributionRestModel> {
    private final Logger logger = LoggerFactory.getLogger(EmailGroupDistributionConfigActions.class);
    private final JpaRepository<EmailGroupDistributionConfigEntity, Long> emailGroupDistributionRepository;

    @Autowired
    public EmailGroupDistributionConfigActions(final CommonDistributionRepository commonDistributionRepository, final JpaRepository<EmailGroupDistributionConfigEntity, Long> repository, final ObjectTransformer objectTransformer) {
        super(EmailGroupDistributionConfigEntity.class, EmailGroupDistributionRestModel.class, commonDistributionRepository, repository, objectTransformer);
        this.emailGroupDistributionRepository = repository;
    }

    @Override
    public EmailGroupDistributionRestModel constructRestModel(final EmailGroupDistributionConfigEntity entity) throws AlertException {
        final EmailGroupDistributionConfigEntity emailGroupEntity = emailGroupDistributionRepository.findOne(entity.getId());
        final CommonDistributionConfigEntity commonEntity = commonDistributionRepository.findByDistributionConfigId(entity.getId());
        if (emailGroupEntity != null && commonEntity != null) {
            final EmailGroupDistributionRestModel restModel = objectTransformer.databaseEntityToConfigRestModel(commonEntity, EmailGroupDistributionRestModel.class);
            restModel.setId(objectTransformer.objectToString(commonEntity.getId()));
            restModel.setGroupName(emailGroupEntity.getGroupName());
            return restModel;
        }
        return null;
    }

    @Override
    public String channelTestConfig(final EmailGroupDistributionRestModel restModel) throws IntegrationException {
        // TODO test config
        return null;
    }

}
