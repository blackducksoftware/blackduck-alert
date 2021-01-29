/*
 * channel
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.alert.channel.msteams;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.exception.IntegrationException;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

@Component
public class MsTeamsEventParser {
    // There is a size limit in the request size that is allowed (20KB). This text limit is meant to hopefully keep the message under that size limit
    private static final int MAX_TEXT_LIMIT_REQUEST = 10000;

    private FreemarkerTemplatingService freemarkerTemplatingService;
    private Configuration freemarkerConfiguration;
    private Template msTeamsTemplate;

    @Autowired
    public MsTeamsEventParser(FreemarkerTemplatingService freemarkerTemplatingService) throws IntegrationException {
        this.freemarkerTemplatingService = freemarkerTemplatingService;
        TemplateLoader msTeamsLoader = freemarkerTemplatingService.createClassTemplateLoader("/templates/msteams");
        this.freemarkerConfiguration = freemarkerTemplatingService.createFreemarkerConfig(msTeamsLoader);
        try {
            this.msTeamsTemplate = freemarkerConfiguration.getTemplate("message_content.ftl");
        } catch (IOException e) {
            throw new IntegrationException("Unable to load the MS Teams template - is it on the classpath? (" + e.getMessage() + ")", e);
        }
    }

    public List<MsTeamsMessage> splitMessages(MsTeamsMessage msTeamsMessage) {
        List<MsTeamsMessage> splitMessages = new LinkedList<>();
        int totalMessageSize = 0;
        List<MsTeamsSection> msTeamsSections = new LinkedList<>();
        for (MsTeamsSection section : msTeamsMessage.getSections()) {
            MsTeamsSection msTeamsSection = new MsTeamsSection();
            msTeamsSection.setSubTopic(section.getSubTopic());
            List<String> componentsMessage = new LinkedList<>();
            for (String message : section.getComponentsMessage()) {
                int messageLength = message.length();
                if (totalMessageSize + messageLength >= MAX_TEXT_LIMIT_REQUEST) {
                    msTeamsSection.setComponentsMessage(componentsMessage);
                    msTeamsSections.add(msTeamsSection);
                    MsTeamsMessage newMessage = new MsTeamsMessage(msTeamsMessage.getTitle(), msTeamsMessage.getTopic(), msTeamsSections);
                    splitMessages.add(newMessage);
                    componentsMessage = new LinkedList<>();
                    msTeamsSections = new LinkedList<>();
                    msTeamsSection = new MsTeamsSection();
                    msTeamsSection.setSubTopic(section.getSubTopic());
                    totalMessageSize = 0;
                }
                componentsMessage.add(message);
                totalMessageSize += messageLength;
            }
            msTeamsSection.setComponentsMessage(componentsMessage);
            msTeamsSections.add(msTeamsSection);
        }

        MsTeamsMessage newMessage = new MsTeamsMessage(msTeamsMessage.getTitle(), msTeamsMessage.getTopic(), msTeamsSections);
        splitMessages.add(newMessage);

        int messagesSize = splitMessages.size();
        if (messagesSize > 1) {
            for (int i = 0; i < messagesSize; i++) {
                MsTeamsMessage message = splitMessages.get(i);
                String title = message.getTitle();
                String newTitle = String.format("%s (%s/%s)", title, i + 1, messagesSize);
                message.setTitle(newTitle);
            }
        }

        return splitMessages;
    }

    public String toJson(MsTeamsMessage msTeamsMessage) throws IntegrationException {
        return freemarkerTemplatingService.resolveTemplate(msTeamsMessage, msTeamsTemplate);
    }

}
