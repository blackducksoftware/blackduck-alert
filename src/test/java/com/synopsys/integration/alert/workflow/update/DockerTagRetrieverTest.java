package com.synopsys.integration.alert.workflow.update;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.util.TestTags;
import com.synopsys.integration.alert.workflow.scheduled.update.DockerTagRetriever;
import com.synopsys.integration.alert.workflow.scheduled.update.model.DockerTagModel;
import com.synopsys.integration.alert.workflow.scheduled.update.model.DockerTagsResponseModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.request.Response;

public class DockerTagRetrieverTest {
    private static final int TAGS_COUNT = 2;

    private final Gson gson = new Gson();

    @Test
    public void getTagsModelTest() throws IntegrationException {
        final IntHttpClient intHttpClient = Mockito.mock(IntHttpClient.class);

        final Response mockResponse = createMockResponse();
        Mockito.when(intHttpClient.execute(Mockito.any(Request.class))).thenReturn(mockResponse);

        final DockerTagRetriever dockerTagRetriever = new DockerTagRetriever(gson, intHttpClient);
        final DockerTagsResponseModel tagsModel = dockerTagRetriever.getTagsModel();
        assertEquals(TAGS_COUNT, tagsModel.getCount());
        assertEquals(TAGS_COUNT, tagsModel.getResults().size());
    }

    @Test
    @Tags({
        @Tag(TestTags.DEFAULT_INTEGRATION),
        @Tag(TestTags.CUSTOM_EXTERNAL_CONNECTION)
    })
    public void getTagsModelTestIT() {
        final IntLogger intLogger = new PrintStreamIntLogger(System.out, LogLevel.INFO);
        final IntHttpClient intHttpClient = new IntHttpClient(intLogger, 10, true, ProxyInfo.NO_PROXY_INFO);

        final Request testRequest = new Request.Builder("https://google.com").build();
        try (final Response googleResponse = intHttpClient.execute(testRequest)) {
            googleResponse.throwExceptionForError();
        } catch (final IntegrationException | IOException e) {
            assumeTrue(null == e, "Could not connect. Skipping this test...");
        }

        final DockerTagRetriever dockerTagRetriever = new DockerTagRetriever(gson, intHttpClient);
        final DockerTagsResponseModel tagsModel = dockerTagRetriever.getTagsModel();
        assertFalse(tagsModel.isEmpty(), "Expected tags from the docker repo to exist");
    }

    private Response createMockResponse() throws IntegrationException {
        final Response mockResponse = Mockito.mock(Response.class);

        final List<DockerTagModel> tagModels = List.of(createDockerTagModel("1.0.0"), createDockerTagModel("1.0.1"));
        final DockerTagsResponseModel mockDockerTagsResponseModel = new DockerTagsResponseModel(TAGS_COUNT, null, null, tagModels);

        final String jsonString = gson.toJson(mockDockerTagsResponseModel);
        Mockito.when(mockResponse.getContentString()).thenReturn(jsonString);
        Mockito.doNothing().when(mockResponse).throwExceptionForError();

        return mockResponse;
    }

    private DockerTagModel createDockerTagModel(final String tagName) {
        return new DockerTagModel(tagName, 1L, List.of(), 1L, 1L, 1L, 1L, "<date>", true);
    }

}
