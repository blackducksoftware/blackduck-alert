package com.synopsys.integration.alert.update;

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
import com.synopsys.integration.alert.test.common.TestTags;
import com.synopsys.integration.alert.update.model.DockerTagModel;
import com.synopsys.integration.alert.update.model.DockerTagsResponseModel;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class DockerTagRetrieverTest {
    private static final int TAGS_COUNT = 2;

    private final Gson gson = new Gson();

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

        HttpUrl httpUrl = new HttpUrl("https://google.com");
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
