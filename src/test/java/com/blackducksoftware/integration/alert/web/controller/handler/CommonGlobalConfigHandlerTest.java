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
package com.blackducksoftware.integration.alert.web.controller.handler;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.alert.ObjectTransformer;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubConfigEntity;
import com.blackducksoftware.integration.alert.database.provider.blackduck.GlobalHubRepository;
import com.blackducksoftware.integration.alert.web.provider.hub.GlobalHubConfigActions;
import com.blackducksoftware.integration.alert.web.provider.hub.GlobalHubConfigRestModel;

public class CommonGlobalConfigHandlerTest {
    private final ObjectTransformer objectTransformer = new ObjectTransformer();

    @Test
    public void postConfigWhenAlreadyExistsTest() {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonGlobalConfigHandler<GlobalHubConfigEntity, GlobalHubConfigRestModel, GlobalHubRepository> handler = new CommonGlobalConfigHandler<>(GlobalHubConfigEntity.class, GlobalHubConfigRestModel.class, configActions,
                objectTransformer);

        final GlobalHubRepository repository = Mockito.mock(GlobalHubRepository.class);
        Mockito.when(configActions.getRepository()).thenReturn(repository);
        Mockito.when(repository.findAll()).thenReturn(Arrays.asList(null, null));

        final ResponseEntity<String> response = handler.postConfig(null);
        assertEquals(HttpStatus.PRECONDITION_FAILED, response.getStatusCode());
    }

    @Test
    public void postConfigWhenDoesNotExistsTest() {
        final GlobalHubConfigActions configActions = Mockito.mock(GlobalHubConfigActions.class);
        final CommonGlobalConfigHandler<GlobalHubConfigEntity, GlobalHubConfigRestModel, GlobalHubRepository> handler = new CommonGlobalConfigHandler<>(GlobalHubConfigEntity.class, GlobalHubConfigRestModel.class, configActions,
                objectTransformer);

        final GlobalHubRepository repository = Mockito.mock(GlobalHubRepository.class);
        Mockito.when(configActions.getRepository()).thenReturn(repository);
        Mockito.when(repository.findAll()).thenReturn(Collections.emptyList());

        final ResponseEntity<String> response = handler.postConfig(null);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

}
