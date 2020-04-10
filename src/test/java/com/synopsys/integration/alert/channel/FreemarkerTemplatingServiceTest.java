package com.synopsys.integration.alert.channel;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.channel.template.FreemarkerTemplatingService;
import com.synopsys.integration.alert.util.TestAlertProperties;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;

public class FreemarkerTemplatingServiceTest {

    @Test
    public void testLoadingByClass() throws IOException {
        TestAlertProperties testAlertProperties = new TestAlertProperties();
        FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(testAlertProperties);
        TemplateLoader templateLoader = freemarkerTemplatingService.createClassTemplateLoader("/freemarker");
        Configuration configuration = freemarkerTemplatingService.createFreemarkerConfig(templateLoader);
        Template test1 = configuration.getTemplate("namespace_1/template_1.ftl");
        Template test2 = configuration.getTemplate("namespace_2/template_1.ftl");
        assertNotNull(test1);
        assertNotNull(test2);
    }

}
