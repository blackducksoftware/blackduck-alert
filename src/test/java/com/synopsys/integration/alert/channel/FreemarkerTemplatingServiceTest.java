package com.synopsys.integration.alert.channel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import freemarker.cache.TemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.channel.util.FreemarkerTemplatingService;
import com.synopsys.integration.alert.util.TestAlertProperties;

import java.io.IOException;

public class FreemarkerTemplatingServiceTest {
    @Test
    public void testExpectedDirectoryPaths() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        final String directory = "directory";
        testAlertProperties.setAlertTemplatesDir(directory);
        final FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(testAlertProperties);

        final String testChannel = "testChannel";

        final String templatePath = freemarkerTemplatingService.getTemplatePath(testChannel);
        assertEquals(directory + "/" + testChannel, templatePath);
    }

    @Test
    public void testEmptyDirectoryPath() {
        final TestAlertProperties testAlertProperties = new TestAlertProperties();
        testAlertProperties.setAlertTemplatesDir("");
        final FreemarkerTemplatingService freemarkerTemplatingService = new FreemarkerTemplatingService(testAlertProperties);

        final String testChannel = "testChannel";
        final String templatePathMissingDirectory = freemarkerTemplatingService.getTemplatePath(testChannel);
        final String userDirectory = System.getProperties().getProperty("user.dir");
        assertEquals(userDirectory + "/src/main/resources/" + testChannel + "/templates", templatePathMissingDirectory);
    }

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
