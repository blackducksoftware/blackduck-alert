/**
 * alert-database
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
package com.synopsys.integration.alert.database.configuration.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.database.configuration.ConfigGroupEntity;

@Component
public interface ConfigGroupRepository extends JpaRepository<ConfigGroupEntity, Long> {
    List<ConfigGroupEntity> findByJobId(UUID jobId);

    //TODO Gavin can help turn this into a valid query
    //    @Query("SELECT job"
    //               + " FROM ConfigGroupEntity job"
    //               + "   INNER JOIN job.descriptorConfigEntity descConf ON job.configId = descConf.id"
    //               + "   LEFT JOIN descConf.fieldValueEntities fieldValue ON descConf.id = fieldValues.configId"
    //               + "   WHERE fieldValue.fieldId = GET_FIELD_ID('" + ChannelDistributionUIConfig.KEY_FREQUENCY + "')"
    //               + "   AND fieldValue.value = :frequency"
    //               + "   WHERE fieldValue.fieldId = GET_FIELD_ID('" + ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME + "')"
    //               + "   AND fieldValue.value = :providerConfigName"
    //               + "   WHERE fieldValue.fieldId = GET_FIELD_ID('" + ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES + "')"
    //               + "   AND fieldValue.value = :notificationType"
    //               + "   WHERE fieldValue.fieldId = GET_FIELD_ID('" + ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT + "')"
    //               + "   AND fieldValue.value = :filterByProject"
    //               + "   WHERE fieldValue.fieldId = GET_FIELD_ID('" + ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT + "')"
    //               + "   AND fieldValue.value = :projectName"
    //               + "   WHERE fieldValue.fieldId = GET_FIELD_ID('" + ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN + "')"
    //               + "   AND fieldValue.value REGEXP :projectNamePattern"
    //    )
    //    List<ConfigGroupEntity> findMatchingJobs(@Param("frequency") String frequency, @Param("providerConfigName") String providerConfigName, @Param("notificationType") String notificationType);

    //    @Query("SELECT job"
    //               + " FROM ConfigGroupEntity job"
    //               + "   INNER JOIN job.descriptorConfigEntity descConf ON job.configId = descConf.id"
    //               + "   LEFT JOIN descConf.fieldValueEntities fieldValue ON descConf.id = fieldValues.configId"
    //               + "   WHERE fieldValue.fieldId = GET_FIELD_ID('" + ProviderDescriptor.KEY_PROVIDER_CONFIG_NAME + "')"
    //               + "   AND fieldValue.value = :providerConfigName"
    //               + "   WHERE fieldValue.fieldId = GET_FIELD_ID('" + ProviderDistributionUIConfig.KEY_NOTIFICATION_TYPES + "')"
    //               + "   AND fieldValue.value = :notificationType"
    //               + "   WHERE fieldValue.fieldId = GET_FIELD_ID('" + ProviderDistributionUIConfig.KEY_FILTER_BY_PROJECT + "')"
    //               + "   AND fieldValue.value = :filterByProject"
    //               + "   WHERE fieldValue.fieldId = GET_FIELD_ID('" + ProviderDistributionUIConfig.KEY_CONFIGURED_PROJECT + "')"
    //               + "   AND fieldValue.value = :projectName"
    //               + "   WHERE fieldValue.fieldId = GET_FIELD_ID('" + ProviderDistributionUIConfig.KEY_PROJECT_NAME_PATTERN + "')"
    //               + "   AND fieldValue.value REGEXP :projectNamePattern"
    //    )
    //    List<ConfigGroupEntity> findMatchingJobs(@Param("providerConfigName") String providerConfigName, @Param("notificationType") String notificationType);
}

