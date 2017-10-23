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

import com.blackducksoftware.integration.hub.alert.channel.email.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailRepository;
import com.google.gson.Gson;

@RestController
public class EmailConfigController {
    private final EmailRepository emailRepository;
    private final Gson gson;

    @Autowired
    EmailConfigController(final EmailRepository emailRepository, final Gson gson) {
        this.emailRepository = emailRepository;
        this.gson = gson;
    }

    @GetMapping(value = "/config/email")
    public List<EmailConfigEntity> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        if (id != null) {
            final EmailConfigEntity foundEntity = emailRepository.findOne(id);
            if (foundEntity != null) {
                return Arrays.asList(foundEntity);
            } else {
                return Collections.emptyList();
            }
        }
        return emailRepository.findAll();
    }

    @PostMapping(value = "/configuration/email")
    public ResponseEntity<String> postConfig(@RequestAttribute(value = "emailConfig", required = true) @RequestBody final EmailConfigEntity emailConfig) {
        if (emailConfig.getId() == null || !emailRepository.exists(emailConfig.getId())) {
            URI uri;
            try {
                uri = new URI("/configuration/email");
            } catch (final URISyntaxException e) {
                return ResponseEntity.status(500).body(e.getMessage());
            }
            final EmailConfigEntity createdEntity = emailRepository.save(emailConfig);
            return ResponseEntity.created(uri).body("\"id\" : " + createdEntity.getId());
        }
        return ResponseEntity.status(409).body("Invalid id");
    }

    @PutMapping(value = "/configuration/email")
    public ResponseEntity<String> putConfig(@RequestAttribute(value = "emailConfig", required = true) @RequestBody final EmailConfigEntity emailConfig) {
        if (emailConfig.getId() != null && emailRepository.exists(emailConfig.getId())) {
            URI uri;
            try {
                uri = new URI("/configuration/email");
            } catch (final URISyntaxException e) {
                return ResponseEntity.status(500).body("error: " + e.getMessage());
            }
            emailRepository.save(emailConfig);
            return ResponseEntity.created(uri).build();
        }
        return ResponseEntity.badRequest().body("No configuration with id " + emailConfig.getId());
    }
}
