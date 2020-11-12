/**
 * web
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
package com.synopsys.integration.alert.web.documentation;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
    public static final String SWAGGER_DEFAULT_PATH_SPEC = "/swagger-ui/";

    // These must be lower-case in order for Swagger to accept them
    private static final String[] SUPPORTED_SUBMIT_METHODS = new String[] {
        "get"
    };

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                   .select()
                   // TODO eventually only expose the "public" api package(s)
                   .apis(RequestHandlerSelectors.basePackage("com.synopsys.integration.alert.web.api"))
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
