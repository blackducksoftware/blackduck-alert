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
package com.synopsys.integration.alert.provider.blackduck.controller;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.controller.ResponseFactory;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckDataActions;
import com.synopsys.integration.alert.web.provider.blackduck.BlackDuckDataController;

public class BlackDuckDataControllerTest {
    private final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());

    @Test
    public void testGetHubProjects() {
        final BlackDuckDataActions blackDuckDataActions = Mockito.mock(BlackDuckDataActions.class);
        Mockito.when(blackDuckDataActions.getBlackDuckProjects()).thenReturn(Collections.emptyList());
        ResponseFactory responseFactory = new ResponseFactory();
        final BlackDuckDataController blackDuckDataHandler = new BlackDuckDataController(responseFactory, blackDuckDataActions, contentConverter);
        final ResponseEntity<String> responseEntity = blackDuckDataHandler.getProjects();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("{\"id\":\"-1\",\"message\":\"[]\"}", responseEntity.getBody());
    }

    @Test
    public void testGetHubProjectsThrowException() {
        final BlackDuckDataActions blackDuckDataActions = Mockito.mock(BlackDuckDataActions.class);
        Mockito.when(blackDuckDataActions.getBlackDuckProjects()).thenThrow(new IllegalStateException("ErrorMessage"));
        ResponseFactory responseFactory = new ResponseFactory();
        final BlackDuckDataController blackDuckDataHandler = new BlackDuckDataController(responseFactory, blackDuckDataActions, contentConverter);
        final ResponseEntity<String> responseEntity = blackDuckDataHandler.getProjects();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("{\"id\":\"-1\",\"message\":\"ErrorMessage\"}", responseEntity.getBody());
    }

}
