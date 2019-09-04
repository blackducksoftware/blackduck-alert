/**
 * blackduck-alert
 *
 * Copyright (c) 2019 Synopsys, Inc.
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

import com.synopsys.integration.alert.channel.util.FreemarkerTemplatingService;
import com.synopsys.integration.alert.common.event.DistributionEvent;
import com.synopsys.integration.alert.common.message.model.MessageContentGroup;
import com.synopsys.integration.exception.IntegrationException;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.Session;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.function.Function;

@Component
public class MsTeamsEventParser {
    private FreemarkerTemplatingService freemarkerTemplatingService;
    private Configuration freemarkerConfiguration;
    private Template msTeamsTemplate;

    @Autowired
    public MsTeamsEventParser(FreemarkerTemplatingService freemarkerTemplatingService) throws IntegrationException {
        this.freemarkerTemplatingService = freemarkerTemplatingService;
        //TODO ekerwin - when we fix email template loading, we can consolidate the configurations to just load /templates
        TemplateLoader msTeamsLoader = freemarkerTemplatingService.createClassTemplateLoader("/templates/channel/msteams");
        this.freemarkerConfiguration = freemarkerTemplatingService.createFreemarkerConfig(msTeamsLoader);
        try {
            this.msTeamsTemplate = freemarkerConfiguration.getTemplate("message_content.ftl");
        } catch (IOException e) {
            throw new IntegrationException("Unable to load the MS Teams template - is it on the classpath? (" + e.getMessage() + ")", e);
        }
    }

    public String toJson(DistributionEvent distributionEvent) throws IntegrationException {
        MsTeamsMessage msTeamsMessage = createMessage(distributionEvent);
        return toJson(msTeamsMessage);
    }

    public MsTeamsMessage createMessage(DistributionEvent distributionEvent) {
        return distributionEvent
                .getContent()
                .getSubContent()
                .stream()
                .collect(MsTeamsMessage::new, MsTeamsMessage::addContent, MsTeamsMessage::addAllContent);
    }

    public String toJson(MsTeamsMessage msTeamsMessage) throws IntegrationException {
        return freemarkerTemplatingService.resolveTemplate(msTeamsMessage, msTeamsTemplate);
    }

}
