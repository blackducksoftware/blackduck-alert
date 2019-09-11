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

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.database.api.DefaultProviderDataAccessor;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.provider.blackduck.mock.MockProviderDataAccessor;
import com.synopsys.integration.alert.web.provider.ProviderDataController;

public class ProviderDataControllerTest {
    private static final BlackDuckProviderKey BLACK_DUCK_PROVIDER_KEY = new BlackDuckProviderKey();
    private final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());

    @Test
    public void testGetHubProjects() {
        final DefaultProviderDataAccessor providerDataAccessor = new MockProviderDataAccessor();
        final ResponseFactory responseFactory = new ResponseFactory();
        final ProviderDataController blackDuckDataHandler = new ProviderDataController(responseFactory, providerDataAccessor, contentConverter);
        final ResponseEntity<String> responseEntity = blackDuckDataHandler.getProjects(BLACK_DUCK_PROVIDER_KEY.getUniversalKey());
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("[]", responseEntity.getBody());
    }

    @Test
    public void testGetHubProjectsThrowException() {
        final DefaultProviderDataAccessor providerDataAccessor = Mockito.mock(DefaultProviderDataAccessor.class);
        Mockito.when(providerDataAccessor.findByProviderName(BLACK_DUCK_PROVIDER_KEY.getUniversalKey())).thenThrow(new IllegalStateException("ErrorMessage"));
        final ResponseFactory responseFactory = new ResponseFactory();
        final ProviderDataController blackDuckDataHandler = new ProviderDataController(responseFactory, providerDataAccessor, contentConverter);
        final ResponseEntity<String> responseEntity = blackDuckDataHandler.getProjects(BLACK_DUCK_PROVIDER_KEY.getUniversalKey());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("{\"message\":\"ErrorMessage\"}", responseEntity.getBody());
    }

}
