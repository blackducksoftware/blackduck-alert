package com.synopsys.integration.alert.workflow.update;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.synopsys.integration.alert.workflow.update.model.DockerTagsResponseModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class DockerTagRetriever {
    public static final String ALERT_DOCKER_REGISTRY_URL = "https://hub.docker.com";
    public static final String ALERT_ORGANIZATION_NAME = "blackducksoftware";
    public static final String ALERT_REPOSITORY_NAME = "blackduck-alert";

    private final Logger logger = LoggerFactory.getLogger(DockerTagRetriever.class);

    private final Gson gson;
    private final IntHttpClient intHttpClient;

    public DockerTagRetriever(final Gson gson, final IntHttpClient intHttpClient) {
        this.gson = gson;
        this.intHttpClient = intHttpClient;
    }

    public DockerTagsResponseModel getNextPage(final DockerTagsResponseModel currentModel) {
        if (null != currentModel && currentModel.hasNextPage()) {
            return getTagResponseModel(currentModel.getNextPageUrl());
        }
        return DockerTagsResponseModel.EMPTY;
    }

    public DockerTagsResponseModel getTagsModel() {
        final String tagsUrl = String.format("%s/v2/repositories/%s/%s/tags", ALERT_DOCKER_REGISTRY_URL, ALERT_ORGANIZATION_NAME, ALERT_REPOSITORY_NAME);
        return getTagResponseModel(tagsUrl);
    }

    public String getRepositoryUrl() {
        return String.format("%s/r/%s/%s", ALERT_DOCKER_REGISTRY_URL, ALERT_ORGANIZATION_NAME, ALERT_REPOSITORY_NAME);
    }

    private DockerTagsResponseModel getTagResponseModel(final String pageUrl) {
        final Request dockerTagsRequest = new Request.Builder(pageUrl).build();

        try (final Response tagsResponse = intHttpClient.execute(dockerTagsRequest)) {
            tagsResponse.throwExceptionForError();
            return gson.fromJson(tagsResponse.getContentString(), DockerTagsResponseModel.class);
        } catch (final IOException | IntegrationException e) {
            logger.debug("Could not get docker tags from {}: {}", pageUrl, e.getMessage());
        }
        return DockerTagsResponseModel.EMPTY;
    }

}
