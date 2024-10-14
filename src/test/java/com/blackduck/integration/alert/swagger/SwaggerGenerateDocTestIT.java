/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.swagger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.Charset;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.blackduck.integration.alert.common.rest.AlertRestConstants;
import com.blackduck.integration.alert.test.common.TestResourceUtils;
import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.alert.util.AlertIntegrationTest;
import com.blackduck.integration.alert.util.AlertIntegrationTestConstants;
import com.blackduck.integration.alert.web.documentation.SwaggerConfiguration;

@AlertIntegrationTest
public class SwaggerGenerateDocTestIT {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).apply(SecurityMockMvcConfigurers.springSecurity()).build();
    }

    @Test
    @Tag(TestTags.CUSTOM_SWAGGER)
    @WithMockUser(roles = AlertIntegrationTestConstants.ROLE_ALERT_ADMIN)
    void testGenerateSwaggerAPISpec() {
        MockHttpServletRequestBuilder request = MockMvcRequestBuilders.get(AlertRestConstants.SWAGGER_PATH)
            .with(SecurityMockMvcRequestPostProcessors.user("admin").roles(AlertIntegrationTestConstants.ROLE_ALERT_ADMIN))
            .with(SecurityMockMvcRequestPostProcessors.csrf());
        String swaggerAPISpecContent = assertDoesNotThrow(() -> mockMvc.perform(request)
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andReturn()
            .getResponse()
            .getContentAsString()
        );

        Assertions.assertTrue(swaggerAPISpecContent.contains(SwaggerConfiguration.SWAGGER_DESCRIPTION));

        Path swaggerAPISpecPath = assertDoesNotThrow(TestResourceUtils::createSwaggerAPISpecCanonicalFilePath);
        assertDoesNotThrow(() -> FileUtils.write(swaggerAPISpecPath.toFile(), swaggerAPISpecContent, Charset.defaultCharset(), false));
        assertTrue(swaggerAPISpecPath.toFile().exists() && swaggerAPISpecPath.toFile().isFile());

        logger.info("Swagger API spec file successfully created");
        logger.info("  --> " + swaggerAPISpecPath);
    }
}
