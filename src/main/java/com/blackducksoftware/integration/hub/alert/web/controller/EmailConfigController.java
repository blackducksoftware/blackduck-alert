/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
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

import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailRepository;
import com.blackducksoftware.integration.hub.alert.web.model.EmailConfigRestModel;

@RestController
public class EmailConfigController {
    private final EmailRepository emailRepository;

    @Autowired
    EmailConfigController(final EmailRepository emailRepository) {
        this.emailRepository = emailRepository;
    }

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

    private EmailConfigEntity restModelToDatabaseModel(final EmailConfigRestModel restModel) {
        final EmailConfigEntity databaseModel = new EmailConfigEntity(restModel.getId(), restModel.getMailSmtpHost(), restModel.getMailSmtpUser(), restModel.getMailSmtpPassword(), restModel.getMailSmtpPort(),
                restModel.getMailSmtpConnectionTimeout(), restModel.getMailSmtpTimeout(), restModel.getMailSmtpFrom(), restModel.getMailSmtpLocalhost(), restModel.getMailSmtpEhlo(), restModel.getMailSmtpAuth(),
                restModel.getMailSmtpDnsNotify(), restModel.getMailSmtpDsnRet(), restModel.getMailSmtpAllow8bitmime(), restModel.getMailSmtpSendPartial(), restModel.getEmailTemplateDirectory(), restModel.getEmailTemplateLogoImage());
        return databaseModel;
    }

    private EmailConfigRestModel databaseModelToRestModel(final EmailConfigEntity databaseModel) {
        final EmailConfigRestModel restModel = new EmailConfigRestModel(databaseModel.getId(), databaseModel.getMailSmtpHost(), databaseModel.getMailSmtpUser(), databaseModel.getMailSmtpPassword(), databaseModel.getMailSmtpPort(),
                databaseModel.getMailSmtpConnectionTimeout(), databaseModel.getMailSmtpTimeout(), databaseModel.getMailSmtpFrom(), databaseModel.getMailSmtpLocalhost(), databaseModel.getMailSmtpEhlo(), databaseModel.getMailSmtpAuth(),
                databaseModel.getMailSmtpDnsNotify(), databaseModel.getMailSmtpDsnRet(), databaseModel.getMailSmtpAllow8bitmime(), databaseModel.getMailSmtpSendPartial(), databaseModel.getEmailTemplateDirectory(),
                databaseModel.getEmailTemplateLogoImage());
        return restModel;
    }

    private List<EmailConfigRestModel> databaseModelsToRestModels(final List<EmailConfigEntity> databaseModels) {
        final List<EmailConfigRestModel> restModels = new ArrayList<>();
        for (final EmailConfigEntity databaseModel : databaseModels) {
            restModels.add(databaseModelToRestModel(databaseModel));
        }
        return restModels;

    }
}
