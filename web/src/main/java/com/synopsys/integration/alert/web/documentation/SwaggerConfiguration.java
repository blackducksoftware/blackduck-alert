/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.documentation;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.UiConfiguration;
import springfox.documentation.swagger.web.UiConfigurationBuilder;

@Configuration
@EnableOpenApi
public class SwaggerConfiguration {
    public static final String SWAGGER_DEFAULT_PATH_SPEC = "swagger-ui";

    // These must be lower-case in order for Swagger to accept them
    private static final String[] SUPPORTED_SUBMIT_METHODS = new String[] {
        "get"
    };

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                   .select()
                   // TODO eventually only expose the "public" api package(s)
                   .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                   .build()
                   .produces(Set.of("application/json"))
                   .consumes(Set.of("application/json"))
                   .ignoredParameterTypes(HttpServletRequest.class, HttpServletResponse.class)
                   .groupName("production")
                   .apiInfo(apiEndpointInfo());
    }

    @Bean
    public UiConfiguration alertSwaggerUiConfiguration() {
        return UiConfigurationBuilder
                   .builder()
                   .supportedSubmitMethods(SUPPORTED_SUBMIT_METHODS)
                   .build();
    }

    private ApiInfo apiEndpointInfo() {
        return new ApiInfoBuilder()
                   .title("BlackDuck Alert - REST API")
                   .description(
                       "The production REST endpoints used by the Alert UI."
                           + " Currently, these are all subject to change between versions."
                           + " A stable, versioned API is coming soon."
                   )
                   .version("preview")
                   .build();
    }

}
