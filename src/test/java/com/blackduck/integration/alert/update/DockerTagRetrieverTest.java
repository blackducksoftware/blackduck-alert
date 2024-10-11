/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.blackduck.integration.alert.test.common.TestTags;
import com.blackduck.integration.alert.update.model.DockerTagModel;
import com.blackduck.integration.alert.update.model.DockerTagsResponseModel;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.exception.IntegrationException;
import com.blackduck.integration.log.IntLogger;
import com.blackduck.integration.log.LogLevel;
import com.blackduck.integration.log.PrintStreamIntLogger;
import com.blackduck.integration.rest.HttpUrl;
import com.blackduck.integration.rest.client.IntHttpClient;
import com.blackduck.integration.rest.proxy.ProxyInfo;
import com.blackduck.integration.rest.request.Request;
import com.blackduck.integration.rest.response.Response;
import com.google.gson.Gson;

public class DockerTagRetrieverTest {
    private static final int TAGS_COUNT = 2;

    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();

    @Test
    public void getTagsModelTest() throws IntegrationException {
        IntHttpClient intHttpClient = Mockito.mock(IntHttpClient.class);

        Response mockResponse = createMockResponse();
        Mockito.when(intHttpClient.execute(Mockito.any(Request.class))).thenReturn(mockResponse);

        DockerTagRetriever dockerTagRetriever = new DockerTagRetriever(gson, intHttpClient);
        DockerTagsResponseModel tagsModel = dockerTagRetriever.getTagsModel();
        assertEquals(TAGS_COUNT, tagsModel.getCount());
        assertEquals(TAGS_COUNT, tagsModel.getResults().size());
    }

    @Test
    @Tags({
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    })
    public void getTagsModelTestIT() throws IntegrationException {
        IntLogger intLogger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        IntHttpClient intHttpClient = new IntHttpClient(intLogger, gson, 10, true, ProxyInfo.NO_PROXY_INFO);

        HttpUrl httpUrl = new HttpUrl("https://blackduck.com");
        Request testRequest = new Request.Builder(httpUrl).build();
        try (Response googleResponse = intHttpClient.execute(testRequest)) {
            googleResponse.throwExceptionForError();
        } catch (IntegrationException | IOException e) {
            assumeTrue(null == e, "Could not connect. Skipping this test...");
        }

        DockerTagRetriever dockerTagRetriever = new DockerTagRetriever(gson, intHttpClient);
        DockerTagsResponseModel tagsModel = dockerTagRetriever.getTagsModel();
        assertFalse(tagsModel.isEmpty(), "Expected tags from the docker repo to exist");
    }

    private Response createMockResponse() throws IntegrationException {
        Response mockResponse = Mockito.mock(Response.class);

        List<DockerTagModel> tagModels = List.of(createDockerTagModel("1.0.0"), createDockerTagModel("1.0.1"));
        DockerTagsResponseModel mockDockerTagsResponseModel = new DockerTagsResponseModel(TAGS_COUNT, null, null, tagModels);

        String jsonString = gson.toJson(mockDockerTagsResponseModel);
        Mockito.when(mockResponse.getContentString()).thenReturn(jsonString);
        Mockito.doNothing().when(mockResponse).throwExceptionForError();

        return mockResponse;
    }

    private DockerTagModel createDockerTagModel(String tagName) {
        return new DockerTagModel(tagName, 1L, 1L, "<date>");
    }

}
