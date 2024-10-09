/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.documentation;

import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfiguration {
    public static final String SWAGGER_DEFAULT_PATH_SPEC = "swagger-ui";
    public static final String SWAGGER_DESCRIPTION = "The production REST endpoints used by the Alert UI."
        + " Currently, these are all subject to change between versions."
        + " A stable, versioned API is coming soon.";

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
            .packagesToScan("com.blackduck.integration.alert")
            .pathsToMatch("/api/**")
            .group("production")
            .build();
    }

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Black Duck Alert - REST API")
                .description(SWAGGER_DESCRIPTION)
                .version("preview"));
    }

    @Bean
    public GlobalOperationCustomizer operationCustomizer() {
        return new CustomOperationNameGenerator();
    }

}
