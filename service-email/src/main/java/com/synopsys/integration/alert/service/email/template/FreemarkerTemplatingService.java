/*
 * service-email
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.service.email.template;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import org.springframework.stereotype.Component;

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
