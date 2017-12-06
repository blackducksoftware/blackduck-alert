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
package com.blackducksoftware.integration.hub.alert.channel;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.blackducksoftware.integration.hub.alert.AlertConstants;
import com.blackducksoftware.integration.hub.alert.channel.email.service.EmailProperties;

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
        final File templateDirectory = findTemplateDirectory();
        cfg.setDirectoryForTemplateLoading(templateDirectory);
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
        cfg.setLogTemplateExceptions(false);

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
