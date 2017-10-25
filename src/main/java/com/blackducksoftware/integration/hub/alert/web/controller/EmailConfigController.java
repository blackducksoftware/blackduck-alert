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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailRepository;
import com.blackducksoftware.integration.hub.alert.web.model.EmailConfigRestModel;

@RestController
public class EmailConfigController implements ChannelController<EmailConfigEntity, EmailConfigRestModel> {
    private final EmailRepository emailRepository;

    @Autowired
    EmailConfigController(final EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

    @Override
    @GetMapping(value = "/configuration/email")
    public List<EmailConfigRestModel> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        if (id != null) {
            final EmailConfigEntity foundEntity = emailRepository.findOne(id);
            if (foundEntity != null) {
                return Arrays.asList(databaseModelToRestModel(foundEntity));
            } else {
                return Collections.emptyList();
            }
        }
        return databaseModelsToRestModels(emailRepository.findAll());
    }

    @Override
    @PostMapping(value = "/configuration/email")
    public ResponseEntity<String> postConfig(@RequestAttribute(value = "emailConfig", required = true) @RequestBody final EmailConfigRestModel emailConfig) {
        if (emailConfig.getId() == null || !emailRepository.exists(emailConfig.getId())) {
            URI uri;
            try {
                uri = new URI("/configuration/email");
            } catch (final URISyntaxException e) {
                return ResponseEntity.status(500).body(e.getMessage());
            }
            final EmailConfigEntity createdEntity = emailRepository.save(restModelToDatabaseModel(emailConfig));
            return ResponseEntity.created(uri).body("\"id\" : " + createdEntity.getId());
        }
        return ResponseEntity.status(409).body("Invalid id");
    }

    @Override
    @PutMapping(value = "/configuration/email")
    public ResponseEntity<String> putConfig(@RequestAttribute(value = "emailConfig", required = true) @RequestBody final EmailConfigRestModel emailConfig) {
        if (emailConfig.getId() != null && emailRepository.exists(emailConfig.getId())) {
            URI uri;
            try {
                uri = new URI("/configuration/email");
            } catch (final URISyntaxException e) {
                return ResponseEntity.status(500).body("error: " + e.getMessage());
            }
            emailRepository.save(restModelToDatabaseModel(emailConfig));
            return ResponseEntity.created(uri).build();
        }
        return ResponseEntity.badRequest().body("No configuration with id " + emailConfig.getId());
    }

    @Override
    public EmailConfigEntity restModelToDatabaseModel(final EmailConfigRestModel restModel) {
        final EmailConfigEntity databaseModel = new EmailConfigEntity(restModel.getId(), restModel.getMailSmtpHost(), restModel.getMailSmtpUser(), restModel.getMailSmtpPassword(), restModel.getMailSmtpPort(),
                restModel.getMailSmtpConnectionTimeout(), restModel.getMailSmtpTimeout(), restModel.getMailSmtpFrom(), restModel.getMailSmtpLocalhost(), restModel.getMailSmtpEhlo(), restModel.getMailSmtpAuth(),
                restModel.getMailSmtpDnsNotify(), restModel.getMailSmtpDsnRet(), restModel.getMailSmtpAllow8bitmime(), restModel.getMailSmtpSendPartial(), restModel.getEmailTemplateDirectory(), restModel.getEmailTemplateLogoImage(),
                restModel.getEmailSubjectLine());
        return databaseModel;
    }

    @Override
    public EmailConfigRestModel databaseModelToRestModel(final EmailConfigEntity databaseModel) {
        final EmailConfigRestModel restModel = new EmailConfigRestModel(databaseModel.getId(), databaseModel.getMailSmtpHost(), databaseModel.getMailSmtpUser(), databaseModel.getMailSmtpPassword(), databaseModel.getMailSmtpPort(),
                databaseModel.getMailSmtpConnectionTimeout(), databaseModel.getMailSmtpTimeout(), databaseModel.getMailSmtpFrom(), databaseModel.getMailSmtpLocalhost(), databaseModel.getMailSmtpEhlo(), databaseModel.getMailSmtpAuth(),
                databaseModel.getMailSmtpDnsNotify(), databaseModel.getMailSmtpDsnRet(), databaseModel.getMailSmtpAllow8bitmime(), databaseModel.getMailSmtpSendPartial(), databaseModel.getEmailTemplateDirectory(),
                databaseModel.getEmailTemplateLogoImage(), databaseModel.getEmailSubjectLine());
        return restModel;
    }

    @Override
    public List<EmailConfigRestModel> databaseModelsToRestModels(final List<EmailConfigEntity> databaseModels) {
        final List<EmailConfigRestModel> restModels = new ArrayList<>();
        for (final EmailConfigEntity databaseModel : databaseModels) {
            restModels.add(databaseModelToRestModel(databaseModel));
        }
        return restModels;

    }

}
