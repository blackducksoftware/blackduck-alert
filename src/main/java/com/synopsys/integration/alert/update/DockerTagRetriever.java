/*
 * blackduck-alert
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.update;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.update.model.DockerTagsResponseModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class DockerTagRetriever {
    public static final String ALERT_DOCKER_REGISTRY_URL = "https://hub.docker.com";
    public static final String ALERT_ORGANIZATION_NAME = "blackducksoftware";
    public static final String ALERT_REPOSITORY_NAME = "blackduck-alert";

    private final Logger logger = LoggerFactory.getLogger(DockerTagRetriever.class);

    private final Gson gson;
    private final IntHttpClient intHttpClient;

    public DockerTagRetriever(Gson gson, IntHttpClient intHttpClient) {
        this.gson = gson;
        this.intHttpClient = intHttpClient;
    }

    public DockerTagsResponseModel getNextPage(DockerTagsResponseModel currentModel) {
        if (null != currentModel && currentModel.hasNextPage()) {
            return getTagResponseModel(currentModel.getNextPageUrl());
        }
        return DockerTagsResponseModel.EMPTY;
    }

    public DockerTagsResponseModel getTagsModel() {
        String tagsUrl = String.format("%s/v2/repositories/%s/%s/tags", ALERT_DOCKER_REGISTRY_URL, ALERT_ORGANIZATION_NAME, ALERT_REPOSITORY_NAME);
        return getTagResponseModel(tagsUrl);
    }

    public String getRepositoryUrl() {
        return String.format("%s/r/%s/%s", ALERT_DOCKER_REGISTRY_URL, ALERT_ORGANIZATION_NAME, ALERT_REPOSITORY_NAME);
    }

    private DockerTagsResponseModel getTagResponseModel(String pageUrl) {
        HttpUrl httpUrl;
        try {
            httpUrl = new HttpUrl(pageUrl);
        } catch (IntegrationException e) {
            logger.warn("Invalid url: " + pageUrl);
            return DockerTagsResponseModel.EMPTY;
        }
        Request dockerTagsRequest = new Request.Builder(httpUrl).build();

        try (Response tagsResponse = intHttpClient.execute(dockerTagsRequest)) {
            tagsResponse.throwExceptionForError();
            return gson.fromJson(tagsResponse.getContentString(), DockerTagsResponseModel.class);
        } catch (IOException | IntegrationException e) {
            logger.warn("Could not get docker tags from {}: {}", pageUrl, e.getMessage());
        }
        return DockerTagsResponseModel.EMPTY;
    }

}
