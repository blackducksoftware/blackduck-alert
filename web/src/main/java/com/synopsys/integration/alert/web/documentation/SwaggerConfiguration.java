/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.documentation;

import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfiguration {
    public static final String SWAGGER_DEFAULT_PATH_SPEC = "swagger-ui";

    // These must be lower-case in order for Swagger to accept them
    private static final String[] SUPPORTED_SUBMIT_METHODS = new String[] {
        "get"
    };

    @Bean
    public GroupedOpenApi api() {
        return GroupedOpenApi.builder()
            .packagesToScan("com.synopsys.integration.alert")
            .pathsToMatch("/api/**")
            .producesToMatch("application/json")
            .consumesToMatch("application/json")
            .group("production")
            .build();

    }

    @Bean
    public OpenAPI springShopOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("Synopsys Alert - REST API")
                .description("The production REST endpoints used by the Alert UI."
                    + " Currently, these are all subject to change between versions."
                    + " A stable, versioned API is coming soon.")
                .version("preview"));
    }

}
