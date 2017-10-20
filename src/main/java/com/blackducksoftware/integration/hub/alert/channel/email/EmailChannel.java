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
package com.blackducksoftware.integration.hub.alert.channel.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.channel.DistributionChannel;
import com.blackducksoftware.integration.hub.alert.channel.email.model.CategoryDataBuilder;
import com.blackducksoftware.integration.hub.alert.channel.email.model.EmailTarget;
import com.blackducksoftware.integration.hub.alert.channel.email.model.ItemData;
import com.blackducksoftware.integration.hub.alert.channel.email.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.channel.email.model.ProjectDataBuilder;
import com.blackducksoftware.integration.hub.alert.channel.email.service.EmailMessagingService;
import com.blackducksoftware.integration.hub.alert.channel.email.service.EmailProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.NotificationEntity;
import com.blackducksoftware.integration.hub.notification.processor.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.google.gson.Gson;

import freemarker.template.TemplateException;

@Component
public class EmailChannel extends DistributionChannel<String> {
    private final static Logger logger = LoggerFactory.getLogger(EmailChannel.class);
    private final Gson gson;

    @Autowired
    public EmailChannel(final Gson gson) {
        this.gson = gson;
    }

    @JmsListener(destination = EmailChannelConfig.CHANNEL_NAME)
    @Override
    public void recieveMessage(final String message) {
        logger.info("Received email event message: {}", message);
        final EmailEvent emailEvent = gson.fromJson(message, EmailEvent.class);
        logger.info("Email event {}", emailEvent);
        handleEvent(emailEvent);
    }

    private void handleEvent(final EmailEvent emailEvent) {
        final Properties properties = new Properties();
        properties.put(EmailProperties.EMAIL_FROM_ADDRESS_KEY, "jrichard@blackducksoftware.com");
        properties.put(EmailProperties.EMAIL_REPLY_TO_ADDRESS_KEY, "jrichard@blackducksoftware.com");
        properties.put(EmailProperties.JAVAMAIL_CONFIG_PREFIX + EmailProperties.JAVAMAIL_HOST_KEY, "mailrelay.dc2.lan");
        properties.put(EmailProperties.EMAIL_TEMPLATE_DIRECTORY, "src/main/resources/email/templates");
        properties.put(EmailProperties.TEMPLATE_VARIABLE_PREFIX + "all.templates.logo.image", "src/main/resources/email/images/Ducky-80.png");

        try {
            final EmailMessagingService emailService = new EmailMessagingService(new EmailProperties(properties));

            final HashMap<String, Object> model = new HashMap<>();
            model.put(EmailProperties.TEMPLATE_KEY_SUBJECT_LINE, "HI!");
            model.put(EmailProperties.TEMPLATE_KEY_EMAIL_CATEGORY, NotificationCategoryEnum.POLICY_VIOLATION.toString());
            model.put(EmailProperties.TEMPLATE_KEY_HUB_SERVER_URL, "Hub server URL");

            final List<ProjectData> dataList = notificationToData(emailEvent.getNotificationEntity());
            model.put(EmailProperties.TEMPLATE_KEY_TOPICS_LIST, dataList);

            model.put(EmailProperties.TEMPLATE_KEY_START_DATE, String.valueOf(System.currentTimeMillis()));
            model.put(EmailProperties.TEMPLATE_KEY_END_DATE, String.valueOf(System.currentTimeMillis()));
            model.put(EmailProperties.TEMPLATE_KEY_USER_FIRST_NAME, "First");
            model.put(EmailProperties.TEMPLATE_KEY_USER_LAST_NAME, "Last Name");

            final EmailTarget emailTarget = new EmailTarget("jrichard@blackducksoftware.com", "digest.ftl", model);

            emailService.sendEmailMessage(emailTarget);
        } catch (final IOException | MessagingException | TemplateException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private List<ProjectData> notificationToData(final NotificationEntity notificationEntity) {
        final List<ProjectData> dataList = new ArrayList<>();

        final HashMap<String, Object> itemModel = new HashMap<>();
        itemModel.put(ItemTypeEnum.COMPONENT.toString(), notificationEntity.getComponentName());
        itemModel.put(ItemTypeEnum.VERSION.toString(), notificationEntity.getComponentVersion());
        itemModel.put(ItemTypeEnum.RULE.toString(), notificationEntity.getPolicyRuleName());
        final ItemData data = new ItemData(itemModel);
        final CategoryDataBuilder categoryBuilder = new CategoryDataBuilder();
        categoryBuilder.addItem(data);
        categoryBuilder.incrementItemCount(1);
        categoryBuilder.setCategoryKey(NotificationCategoryEnum.POLICY_VIOLATION.toString());

        final ProjectDataBuilder projectDataBuilder = new ProjectDataBuilder();
        projectDataBuilder.setProjectName(notificationEntity.getProjectName());
        projectDataBuilder.setProjectVersion(notificationEntity.getProjectVersion());

        projectDataBuilder.addCategoryBuilder(NotificationCategoryEnum.POLICY_VIOLATION, categoryBuilder);
        dataList.add(projectDataBuilder.build());

        return dataList;
    }

}
