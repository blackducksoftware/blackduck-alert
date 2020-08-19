/*
 * Copyright (C) 2018 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.synopsys.integration.alert.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.google.gson.Gson;
import com.synopsys.integration.alert.web.api.metadata.ExposedEndpointsController;

public class ExposedEndpointsControllerTest {
    @Test
    public void getTest() {
        RequestMappingHandlerMapping handlerMapping = Mockito.mock(RequestMappingHandlerMapping.class);
        ExposedEndpointsController controller = new ExposedEndpointsController(new Gson(), handlerMapping);

        String endpoint1 = "/api/test";
        String endpoint2 = "/api/other/test";

        Map<RequestMappingInfo, HandlerMethod> handlerMethods = new HashMap<>();
        RequestMappingInfo info = new RequestMappingInfo(new PatternsRequestCondition(endpoint1, endpoint2), new RequestMethodsRequestCondition(RequestMethod.GET, RequestMethod.POST), null, null, null, null, null);
        handlerMethods.put(info, null);

        Mockito.when(handlerMapping.getHandlerMethods()).thenReturn(handlerMethods);

        ResponseEntity<String> responseEntity = controller.get();
        Assertions.assertTrue(StringUtils.isNotBlank(responseEntity.getBody()), "Expected the response body to contain json");
    }

}
