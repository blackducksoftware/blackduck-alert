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
package com.blackducksoftware.integration.hub.alert.channel;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.repository.JpaRepository;

import com.blackducksoftware.integration.hub.alert.MessageReceiver;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryData;
import com.blackducksoftware.integration.hub.alert.digest.model.ItemData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.event.AbstractChannelEvent;
import com.blackducksoftware.integration.hub.notification.processor.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.google.gson.Gson;

public abstract class DistributionChannel<E extends AbstractChannelEvent, C extends DatabaseEntity> extends MessageReceiver<E> {
    private final static Logger logger = LoggerFactory.getLogger(DistributionChannel.class);
    protected final JpaRepository<C, Long> repository;

    public DistributionChannel(final Gson gson, final JpaRepository repository, final Class<E> clazz) {
        super(gson, clazz);
        this.repository = repository;
    }

    public abstract void sendMessage(final E event, final C config);

    public abstract String testMessage(final C config);

    protected String createHtmlMessage(final ProjectData projectData) {
        final StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<strong>" + projectData.getProjectName() + " > " + projectData.getProjectVersion() + "</strong>");

        final Map<NotificationCategoryEnum, CategoryData> categoryMap = projectData.getCategoryMap();
        if (categoryMap != null) {
            for (final NotificationCategoryEnum category : NotificationCategoryEnum.values()) {
                final CategoryData data = categoryMap.get(category);
                if (data != null) {
                    htmlBuilder.append("<br />- - - - - - - - - - - - - - - - - - - -");
                    htmlBuilder.append("<br />Type: " + data.getCategoryKey());
                    htmlBuilder.append("<br />Number of Changes: " + data.getItemCount());
                    for (final ItemData item : data.getItemList()) {
                        final Map<String, Object> dataSet = item.getDataSet();
                        htmlBuilder.append("<p>  Rule: " + dataSet.get(ItemTypeEnum.RULE.toString()));
                        htmlBuilder.append(" | Component: " + dataSet.get(ItemTypeEnum.COMPONENT.toString()));
                        htmlBuilder.append(" [" + dataSet.get(ItemTypeEnum.VERSION.toString()) + "]</p>");
                    }
                }
            }
        } else {
            htmlBuilder.append("<br /><i>A notification was received, but it was empty.</i>");
        }
        return htmlBuilder.toString();
    }

    protected String createPlaintextMessage(final ProjectData projectData) {
        final String htmlMessage = createHtmlMessage(projectData);
        return StringEscapeUtils.escapeHtml4(htmlMessage);
    }

    public void handleEvent(final E event) {
        final List<C> configurations = repository.findAll();
        for (final C configEntity : configurations) {
            sendMessage(event, configEntity);
        }
    }

    @Override
    public void receiveMessage(final String message) {
        logger.info(String.format("Received %s event message: %s", getClass().getName(), message));
        final E event = getEvent(message);
        logger.info(String.format("%s event %s", getClass().getName(), event));

        handleEvent(event);
    }

}
