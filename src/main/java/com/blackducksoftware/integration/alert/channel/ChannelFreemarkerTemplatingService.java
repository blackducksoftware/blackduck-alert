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
package com.blackducksoftware.integration.alert.channel;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.alert.AlertConstants;
import com.blackducksoftware.integration.alert.channel.email.EmailProperties;

import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.TemplateNotFoundException;

public class ChannelFreemarkerTemplatingService {
    private final Logger logger = LoggerFactory.getLogger(ChannelFreemarkerTemplatingService.class);
    private final String templateDirectory;
    private final Configuration configuration;

    public ChannelFreemarkerTemplatingService(final String templateDirectory) throws IOException {
        this.templateDirectory = templateDirectory;
        this.configuration = createFreemarkerConfig();
    }

    public Configuration createFreemarkerConfig() throws IOException {
        final Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setLogTemplateExceptions(false);

        final File templateDirectory = findTemplateDirectory();
        if (templateDirectory != null) {
            cfg.setDirectoryForTemplateLoading(templateDirectory);
        }
        return cfg;
    }

    private File findTemplateDirectory() {
        try {
            File templateDir = null;
            final String appHomeDir = System.getProperty(AlertConstants.SYSTEM_PROPERTY_KEY_APP_HOME);
            if (StringUtils.isNotBlank(appHomeDir)) {
                templateDir = new File(appHomeDir, "templates");
            }
            if (StringUtils.isNotBlank(templateDirectory)) {
                templateDir = new File(templateDirectory);
            }
            return templateDir;
        } catch (final Exception e) {
            logger.error("Error finding the template directory", e);
            return null;
        }
    }

    public String getResolvedTemplate(final Map<String, Object> model, final String templateName) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
        final StringWriter stringWriter = new StringWriter();
        final Template template = configuration.getTemplate(templateName);
        template.process(model, stringWriter);
        return stringWriter.toString();
    }

    public String getResolvedSubjectLine(final Map<String, Object> model) throws IOException, TemplateException {
        String subjectLine = (String) model.get(EmailProperties.TEMPLATE_KEY_SUBJECT_LINE);
        if (StringUtils.isBlank(subjectLine)) {
            subjectLine = "Default Subject Line - please define one";
        }
        final Template subjectLineTemplate = new Template("subjectLineTemplate", subjectLine, configuration);
        final StringWriter stringWriter = new StringWriter();
        subjectLineTemplate.process(model, stringWriter);
        return stringWriter.toString();
    }

}
