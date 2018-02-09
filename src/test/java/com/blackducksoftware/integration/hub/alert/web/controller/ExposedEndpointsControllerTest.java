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
package com.blackducksoftware.integration.hub.alert.web.controller;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.condition.PatternsRequestCondition;
import org.springframework.web.servlet.mvc.condition.RequestMethodsRequestCondition;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class ExposedEndpointsControllerTest {
    @Test
    public void getTest() {
        final RequestMappingHandlerMapping handlerMapping = Mockito.mock(RequestMappingHandlerMapping.class);
        final ExposedEndpointsController controller = new ExposedEndpointsController(handlerMapping);

        final String endpoint1 = "/api/test";
        final String endpoint2 = "/api/other/test";
        final Set<RequestMethod> expectedSet = new HashSet<>(Arrays.asList(RequestMethod.GET, RequestMethod.POST));
        
        final Map<RequestMappingInfo, HandlerMethod> handlerMethods = new HashMap<>();
        final RequestMappingInfo info = new RequestMappingInfo(new PatternsRequestCondition(endpoint1, endpoint2), new RequestMethodsRequestCondition(RequestMethod.GET, RequestMethod.POST), null, null, null, null, null);
        handlerMethods.put(info, null);

        Mockito.when(handlerMapping.getHandlerMethods()).thenReturn(handlerMethods);

        final Map<String, Set<RequestMethod>> mappings = controller.get();
        assertEquals(mappings.get(endpoint1), expectedSet);
    }

}
