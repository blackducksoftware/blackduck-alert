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

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.config.ui.ChannelDistributionUIConfig;
import com.synopsys.integration.alert.database.configuration.ConfigGroupEntity;

@Component
public interface ConfigGroupRepository extends JpaRepository<ConfigGroupEntity, Long> {
    List<ConfigGroupEntity> findByJobId(UUID jobId);

    @Query("SELECT job"
               + " FROM ConfigGroupEntity job"
               + "   INNER JOIN job.descriptorConfigEntity descConf ON job.configId = descConf.id"
               + "   LEFT JOIN descConf.fieldValueEntities fieldValues ON descConf.id = fieldValues.configId"
               + "   WHERE fieldValues.fieldId = GET_FIELD_ID('" + ChannelDistributionUIConfig.KEY_NAME + "')"
               + "   AND fieldValues.value = :jobName"
    )
    List<ConfigGroupEntity> findByJobName(@Param("jobName") String jobName);

    List<ConfigGroupEntity> findAllByJobIdIn(Collection<UUID> jobIds);

    @Query("SELECT DISTINCT job.jobId"
               + " FROM ConfigGroupEntity job"
               + " WHERE job.jobId NOT IN ("
               + "   SELECT excludedJob.jobId"
               + "   FROM ConfigGroupEntity excludedJob"
               + "   JOIN excludedJob.descriptorConfigEntity descConf ON excludedJob.configId = descConf.id"
               + "   JOIN descConf.registeredDescriptorEntity regDesc ON descConf.descriptorId = regDesc.id"
               + "   WHERE regDesc.name NOT IN :descriptors"
               + " )"
    )
    Page<UUID> findDistinctJobIdsOnlyIncludingProvidedDescriptors(@Param("descriptors") Collection<String> descriptors, Pageable pageable);

}
