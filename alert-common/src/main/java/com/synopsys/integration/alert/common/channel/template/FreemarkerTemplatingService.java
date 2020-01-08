/**
 * alert-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
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
package com.synopsys.integration.alert.common.channel.template;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.AlertConstants;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.exception.IntegrationException;

import freemarker.cache.ClassTemplateLoader;
import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

@Component
public class FreemarkerTemplatingService {
    public static final String KEY_ALERT_SERVER_URL = "alertServerUrl";
    private final Logger logger = LoggerFactory.getLogger(FreemarkerTemplatingService.class);
    private final AlertProperties alertProperties;

    @Autowired
    public FreemarkerTemplatingService(AlertProperties alertProperties) {
        this.alertProperties = alertProperties;
    }

    public String getTemplatePath(String channelDirectory) throws AlertException {
        String templatesDirectory = alertProperties.getAlertTemplatesDir();
        if (StringUtils.isNotBlank(templatesDirectory)) {
            return String.format("%s/%s", templatesDirectory, channelDirectory);
        }
        throw new AlertException(String.format("Could not find the Alert template directory '%s'", templatesDirectory));
    }

    public Configuration createFreemarkerConfig(File templateLoadingDirectory) throws IOException {
        Configuration configuration = createDefaultConfiguration();

        if (templateLoadingDirectory != null) {
            configuration.setDirectoryForTemplateLoading(templateLoadingDirectory);
        }

        return configuration;
    }

    public Configuration createFreemarkerConfig(TemplateLoader templateLoader) {
        Configuration configuration = createDefaultConfiguration();

        configuration.setTemplateLoader(templateLoader);

        return configuration;
    }

    public TemplateLoader createClassTemplateLoader(String basePackagePath) {
        return new ClassTemplateLoader(getClass(), basePackagePath);
    }

    private Configuration createDefaultConfiguration() {
        Configuration cfg = new Configuration(Configuration.VERSION_2_3_25);

        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setLogTemplateExceptions(false);

        return cfg;
    }

    public Configuration createFreemarkerConfig(String templateDirectory) throws IOException {
        File templateLoadingDirectory = findTemplateDirectory(templateDirectory);
        return createFreemarkerConfig(templateLoadingDirectory);
    }

    private File findTemplateDirectory(String templateDirectory) {
        try {
            File templateDir = null;
            String appHomeDir = System.getProperty(AlertConstants.SYSTEM_PROPERTY_KEY_APP_HOME);
            if (StringUtils.isNotBlank(appHomeDir)) {
                templateDir = new File(appHomeDir, "templates");
            }
            if (StringUtils.isNotBlank(templateDirectory)) {
                templateDir = new File(templateDirectory);
            }
            return templateDir;
        } catch (Exception e) {
            logger.error("Error finding the template directory", e);
            return null;
        }
    }

    public String resolveTemplate(Map<String, Object> dataModel, Template template) throws IntegrationException {
        try {
            StringWriter stringWriter = new StringWriter();
            template.process(dataModel, stringWriter);
            return stringWriter.toString();
        } catch (IOException | TemplateException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

    public String resolveTemplate(FreemarkerDataModel dataModel, Template template) throws IntegrationException {
        try {
            StringWriter stringWriter = new StringWriter();
            template.process(dataModel, stringWriter);
            return stringWriter.toString();
        } catch (IOException | TemplateException e) {
            throw new IntegrationException(e.getMessage(), e);
        }
    }

}
