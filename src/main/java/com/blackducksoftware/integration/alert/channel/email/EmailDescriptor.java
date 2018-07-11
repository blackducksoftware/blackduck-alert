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
package com.blackducksoftware.integration.alert.channel.email;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.jms.MessageListener;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.ObjectTransformer;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGroupDistributionConfigEntity;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGroupDistributionRepository;
import com.blackducksoftware.integration.alert.channel.email.model.EmailGroupDistributionRestModel;
import com.blackducksoftware.integration.alert.channel.email.model.GlobalEmailConfigEntity;
import com.blackducksoftware.integration.alert.channel.email.model.GlobalEmailConfigRestModel;
import com.blackducksoftware.integration.alert.channel.email.model.GlobalEmailRepository;
import com.blackducksoftware.integration.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.alert.descriptor.ChannelDescriptor;
import com.blackducksoftware.integration.alert.event.ChannelEvent;
import com.blackducksoftware.integration.alert.exception.AlertException;
import com.blackducksoftware.integration.alert.web.model.CommonDistributionConfigRestModel;
import com.blackducksoftware.integration.alert.web.model.ConfigRestModel;
import com.blackducksoftware.integration.exception.IntegrationException;
import com.google.gson.Gson;

@Component
public class EmailDescriptor extends ChannelDescriptor {
    private final EmailGroupChannel emailGroupChannel;
    private final GlobalEmailRepository globalEmailRepository;
    private final EmailGroupDistributionRepository emailGroupDistributionRepository;
    private final Gson gson;
    private final ObjectTransformer objectTransformer;

    @Autowired
    public EmailDescriptor(final EmailGroupChannel emailGroupChannel, final GlobalEmailRepository globalEmailRepository, final EmailGroupDistributionRepository emailGroupDistributionRepository, final Gson gson,
            final ObjectTransformer objectTransformer) {
        super(EmailGroupChannel.COMPONENT_NAME, EmailGroupChannel.COMPONENT_NAME, true);
        this.emailGroupChannel = emailGroupChannel;
        this.globalEmailRepository = globalEmailRepository;
        this.emailGroupDistributionRepository = emailGroupDistributionRepository;
        this.gson = gson;
        this.objectTransformer = objectTransformer;
    }

    @Override
    public List<? extends DatabaseEntity> readDistributionEntities() {
        return emailGroupDistributionRepository.findAll();
    }

    @Override
    public Optional<? extends DatabaseEntity> readDistributionEntity(final long id) {
        return emailGroupDistributionRepository.findById(id);
    }

    @Override
    public Optional<? extends DatabaseEntity> saveDistributionEntity(final DatabaseEntity entity) {
        if (entity instanceof EmailGroupDistributionConfigEntity) {
            final EmailGroupDistributionConfigEntity emailEntity = (EmailGroupDistributionConfigEntity) entity;
            return Optional.of(emailGroupDistributionRepository.save(emailEntity));
        }
        return Optional.empty();
    }

    @Override
    public void deleteDistributionEntity(final long id) {
        emailGroupDistributionRepository.deleteById(id);
    }

    @Override
    public CommonDistributionConfigRestModel convertFromStringToDistributionRestModel(final String json) {
        return gson.fromJson(json, EmailGroupDistributionRestModel.class);
    }

    @Override
    public DatabaseEntity convertFromDistributionRestModelToDistributionConfigEntity(final CommonDistributionConfigRestModel restModel) throws AlertException {
        return objectTransformer.configRestModelToDatabaseEntity(restModel, EmailGroupDistributionConfigEntity.class);
    }

    @Override
    public void validateDistributionConfig(final CommonDistributionConfigRestModel restModel, final Map<String, String> fieldErrors) {
        if (restModel instanceof EmailGroupDistributionRestModel) {
            final EmailGroupDistributionRestModel emailRestModel = (EmailGroupDistributionRestModel) restModel;
            if (StringUtils.isBlank(emailRestModel.getGroupName())) {
                fieldErrors.put("groupName", "A group must be specified.");
            }
        }
    }

    @Override
    public Optional<? extends CommonDistributionConfigRestModel> constructRestModel(final CommonDistributionConfigEntity commonEntity, final DatabaseEntity distributionEntity) throws AlertException {
        if (distributionEntity instanceof EmailGroupDistributionConfigEntity) {
            final EmailGroupDistributionConfigEntity emailEntity = (EmailGroupDistributionConfigEntity) distributionEntity;
            final EmailGroupDistributionRestModel restModel = objectTransformer.databaseEntityToConfigRestModel(commonEntity, EmailGroupDistributionRestModel.class);
            restModel.setId(objectTransformer.objectToString(commonEntity.getId()));
            restModel.setGroupName(emailEntity.getGroupName());
            restModel.setEmailTemplateLogoImage(emailEntity.getEmailTemplateLogoImage());
            restModel.setEmailSubjectLine(emailEntity.getEmailSubjectLine());
            return Optional.ofNullable(restModel);
        }
        return Optional.empty();
    }

    @Override
    public void testDistributionConfig(final CommonDistributionConfigRestModel restModel, final ChannelEvent event) throws IntegrationException {
        final EmailGroupDistributionConfigEntity emailEntity = (EmailGroupDistributionConfigEntity) convertFromDistributionRestModelToDistributionConfigEntity(restModel);
        emailGroupChannel.sendAuditedMessage(event, emailEntity);
    }

    @Override
    public MessageListener getChannelListener() {
        return emailGroupChannel;
    }

    @Override
    public List<? extends DatabaseEntity> readGlobalEntities() {
        return globalEmailRepository.findAll();
    }

    @Override
    public Optional<? extends DatabaseEntity> readGlobalEntity(final long id) {
        return globalEmailRepository.findById(id);
    }

    @Override
    public Optional<? extends DatabaseEntity> saveGlobalEntity(final DatabaseEntity entity) {
        if (entity instanceof GlobalEmailConfigEntity) {
            final GlobalEmailConfigEntity emailEntity = (GlobalEmailConfigEntity) entity;
            return Optional.of(globalEmailRepository.save(emailEntity));
        }
        return Optional.empty();
    }

    @Override
    public void deleteGlobalEntity(final long id) {
        globalEmailRepository.deleteById(id);
    }

    @Override
    public ConfigRestModel convertFromStringToGlobalRestModel(final String json) {
        return gson.fromJson(json, GlobalEmailConfigRestModel.class);
    }

    @Override
    public DatabaseEntity convertFromGlobalRestModelToGlobalConfigEntity(final ConfigRestModel restModel) throws AlertException {
        return objectTransformer.configRestModelToDatabaseEntity(restModel, GlobalEmailConfigEntity.class);
    }

    @Override
    public ConfigRestModel convertFromGlobalEntityToGlobalRestModel(final DatabaseEntity entity) throws AlertException {
        return objectTransformer.databaseEntityToConfigRestModel(entity, GlobalEmailConfigRestModel.class);
    }

    @Override
    public void validateGlobalConfig(final ConfigRestModel restModel, final Map<String, String> fieldErrors) {
        if (restModel instanceof GlobalEmailConfigRestModel) {
            final GlobalEmailConfigRestModel emailRestModel = (GlobalEmailConfigRestModel) restModel;

            if (StringUtils.isNotBlank(emailRestModel.getMailSmtpPort()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpPort())) {
                fieldErrors.put("mailSmtpPort", "Not an Integer.");
            }
            if (StringUtils.isNotBlank(emailRestModel.getMailSmtpConnectionTimeout()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpConnectionTimeout())) {
                fieldErrors.put("mailSmtpConnectionTimeout", "Not an Integer.");
            }
            if (StringUtils.isNotBlank(emailRestModel.getMailSmtpTimeout()) && !StringUtils.isNumeric(emailRestModel.getMailSmtpTimeout())) {
                fieldErrors.put("mailSmtpTimeout", "Not an Integer.");
            }
        }
    }

    @Override
    public void testGlobalConfig(final DatabaseEntity entity) throws IntegrationException {
        throw new IntegrationException("Error, can't test global email configuration");
    }

    @Override
    public Field[] getGlobalEntityFields() {
        return GlobalEmailConfigEntity.class.getDeclaredFields();
    }

    @Override
    public ConfigRestModel getGlobalRestModelObject() {
        return new GlobalEmailConfigRestModel();
    }

}
