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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.blackducksoftware.integration.hub.alert.channel.email.EmailChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailRepository;
import com.blackducksoftware.integration.hub.alert.web.model.EmailConfigRestModel;

@RestController
public class EmailConfigController extends ConfigController<EmailConfigEntity, EmailConfigRestModel> {

    @Autowired
    EmailConfigController(final EmailRepository repository) {
        super(repository);
    }

    @Override
    @GetMapping(value = "/configuration/email")
    public List<EmailConfigRestModel> getConfig(@RequestParam(value = "id", required = false) final Long id) {
        return super.getConfig(id);
    }

    @Override
    @PostMapping(value = "/configuration/email")
    public ResponseEntity<String> postConfig(@RequestAttribute(value = "emailConfig", required = true) @RequestBody final EmailConfigRestModel emailConfig) {
        return super.postConfig(emailConfig);
    }

    @Override
    @PutMapping(value = "/configuration/email")
    public ResponseEntity<String> putConfig(@RequestAttribute(value = "emailConfig", required = true) @RequestBody final EmailConfigRestModel emailConfig) {
        return super.putConfig(emailConfig);
    }

    @Override
    @DeleteMapping(value = "/configuration/email")
    public ResponseEntity<String> deleteConfig(@RequestAttribute(value = "emailConfig", required = true) @RequestBody final EmailConfigRestModel emailConfig) {
        return super.deleteConfig(emailConfig);
    }

    @Override
    @PostMapping(value = "/configuration/email/test")
    public ResponseEntity<String> testConfig(@RequestAttribute(value = "emailConfig", required = true) final EmailConfigRestModel emailConfig) {
        final EmailChannel channel = new EmailChannel(null, null, (EmailRepository) repository);
        final String responseMessage = channel.testMessage(restModelToDatabaseModel(emailConfig));
        return super.createResponse(HttpStatus.OK, emailConfig.getId(), responseMessage);
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

}
