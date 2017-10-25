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
package com.blackducksoftware.integration.hub.alert.web.controller;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.config.AccumulatorConfig;
import com.blackducksoftware.integration.hub.alert.config.DailyDigestBatchConfig;
import com.blackducksoftware.integration.hub.alert.config.RealTimeDigestBatchConfig;
import com.blackducksoftware.integration.hub.alert.datasource.entity.GlobalConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.GlobalRepository;
import com.blackducksoftware.integration.hub.alert.web.model.GlobalConfigRestModel;

@RestController
public class GlobalConfigController implements ConfigController<GlobalConfigEntity, GlobalConfigRestModel> {
    private final GlobalRepository globalRepository;
    private final AccumulatorConfig accumulatorConfig;
    private final RealTimeDigestBatchConfig realTimeDigestBatchConfig;
    private final DailyDigestBatchConfig dailyDigestBatchConfig;

    @Autowired
    GlobalConfigController(final GlobalRepository globalRepository, final AccumulatorConfig accumulatorConfig, final RealTimeDigestBatchConfig realTimeDigestBatchConfig, final DailyDigestBatchConfig dailyDigestBatchConfig) {
        this.globalRepository = globalRepository;
        this.accumulatorConfig = accumulatorConfig;
        this.realTimeDigestBatchConfig = realTimeDigestBatchConfig;
        this.dailyDigestBatchConfig = dailyDigestBatchConfig;
    }

    @Override
    @GetMapping(value = "/configuration/global")
    public List<GlobalConfigRestModel> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        if (id != null) {
            final GlobalConfigEntity foundEntity = globalRepository.findOne(id);
            if (foundEntity != null) {
                return Arrays.asList(databaseModelToRestModel(foundEntity));
            } else {
                return Collections.emptyList();
            }
        }
        return databaseModelsToRestModels(globalRepository.findAll());
    }

    @Override
    @PostMapping(value = "/configuration/global")
    public ResponseEntity<String> postConfig(@RequestAttribute(value = "globalConfig", required = true) @RequestBody final GlobalConfigRestModel globalConfig) {
        final List<GlobalConfigEntity> configs = globalRepository.findAll();
        if (configs != null && !configs.isEmpty()) {
            for (final GlobalConfigEntity config : configs) {
                globalRepository.delete(config);
            }
        }
        URI uri;
        try {
            uri = new URI("/configuration/global");
        } catch (final URISyntaxException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
        final GlobalConfigEntity createdEntity = globalRepository.save(restModelToDatabaseModel(globalConfig));
        if (globalConfig != null) {
            if (StringUtils.isNotBlank(globalConfig.getAccumulatorCron())) {
                accumulatorConfig.scheduleJobExecution(globalConfig.getAccumulatorCron());
            }
            if (StringUtils.isNotBlank(globalConfig.getRealTimeDigestCron())) {
                realTimeDigestBatchConfig.scheduleJobExecution(globalConfig.getRealTimeDigestCron());
            }
            if (StringUtils.isNotBlank(globalConfig.getDailyDigestCron())) {
                dailyDigestBatchConfig.scheduleJobExecution(globalConfig.getDailyDigestCron());
            }
        }
        return ResponseEntity.created(uri).body("\"id\" : " + createdEntity.getId());
    }

    @Override
    @PutMapping(value = "/configuration/global")
    public ResponseEntity<String> putConfig(@RequestAttribute(value = "globalConfig", required = true) @RequestBody final GlobalConfigRestModel globalConfig) {
        final List<GlobalConfigEntity> configs = globalRepository.findAll();
        if (configs != null && !configs.isEmpty()) {
            for (final GlobalConfigEntity config : configs) {
                globalRepository.delete(config);
            }
        }
        URI uri;
        try {
            uri = new URI("/configuration/global");
        } catch (final URISyntaxException e) {
            return ResponseEntity.status(500).body("error: " + e.getMessage());
        }
        globalRepository.save(restModelToDatabaseModel(globalConfig));
        if (globalConfig != null) {
            if (StringUtils.isNotBlank(globalConfig.getAccumulatorCron())) {
                accumulatorConfig.scheduleJobExecution(globalConfig.getAccumulatorCron());
            }
            if (StringUtils.isNotBlank(globalConfig.getRealTimeDigestCron())) {
                realTimeDigestBatchConfig.scheduleJobExecution(globalConfig.getRealTimeDigestCron());
            }
            if (StringUtils.isNotBlank(globalConfig.getDailyDigestCron())) {
                dailyDigestBatchConfig.scheduleJobExecution(globalConfig.getDailyDigestCron());
            }
        }
        return ResponseEntity.created(uri).build();
    }

    @Override
    @DeleteMapping(value = "/configuration/global")
    public ResponseEntity<String> deleteConfig(@RequestAttribute(value = "globalConfig", required = true) @RequestBody final GlobalConfigRestModel globalConfig) {
        if (globalConfig.getId() != null && globalRepository.exists(globalConfig.getId())) {
            globalRepository.delete(globalConfig.getId());
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().body("No configuration with id " + globalConfig.getId());
    }

    @Override
    @PostMapping(value = "/configuration/global/test")
    public ResponseEntity<String> testConfig(@RequestAttribute(value = "globalConfig", required = true) final GlobalConfigRestModel globalConfig) {
        // TODO implement method for testing the configuration
        return ResponseEntity.notFound().build();
    }

    @Override
    public GlobalConfigEntity restModelToDatabaseModel(final GlobalConfigRestModel restModel) {
        final GlobalConfigEntity databaseModel = new GlobalConfigEntity(restModel.getHubUrl(), restModel.getHubTimeout(), restModel.getHubUsername(), restModel.getHubPassword(), restModel.getHubProxyHost(), restModel.getHubProxyPort(),
                restModel.getHubProxyUsername(), restModel.getHubProxyPassword(), restModel.getHubAlwaysTrustCertificate(), restModel.getAccumulatorCron(), restModel.getDailyDigestCron(), restModel.getRealTimeDigestCron());
        return databaseModel;
    }

    @Override
    public GlobalConfigRestModel databaseModelToRestModel(final GlobalConfigEntity databaseModel) {
        final GlobalConfigRestModel restModel = new GlobalConfigRestModel(databaseModel.getHubUrl(), databaseModel.getHubTimeout(), databaseModel.getHubUsername(), databaseModel.getHubPassword(), databaseModel.getHubProxyHost(),
                databaseModel.getHubProxyPort(), databaseModel.getHubProxyUsername(), databaseModel.getHubProxyPassword(), databaseModel.getHubAlwaysTrustCertificate(), databaseModel.getAccumulatorCron(), databaseModel.getDailyDigestCron(),
                databaseModel.getRealTimeDigestCron());
        return restModel;
    }

    @Override
    public List<GlobalConfigRestModel> databaseModelsToRestModels(final List<GlobalConfigEntity> databaseModels) {
        final List<GlobalConfigRestModel> restModels = new ArrayList<>();
        for (final GlobalConfigEntity databaseModel : databaseModels) {
            restModels.add(databaseModelToRestModel(databaseModel));
        }
        return restModels;
    }

}
