package com.synopsys.integration.alert.web.controller;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.common.rest.ResponseFactory;
import com.synopsys.integration.alert.web.actions.AboutActions;
import com.synopsys.integration.alert.web.model.AboutModel;

public class AboutControllerTest {
    private final Gson gson = new Gson();
    ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());

    @Test
    public void testController() {
        String version = "1.2.3";
        String created = "date";
        String description = "description";
        String gitHubUrl = "https://www.google.com";
        boolean initialized = true;
        String startupTime = "startup time is now";
        List<String> providers = List.of("provider_key");
        List<String> channels = List.of("channel_key");

        ResponseFactory responseFactory = new ResponseFactory();
        AboutModel model = new AboutModel(version, created, description, gitHubUrl, initialized, startupTime, providers, channels);
        AboutActions aboutActions = Mockito.mock(AboutActions.class);

        Mockito.when(aboutActions.getAboutModel()).thenReturn(Optional.of(model));
        AboutController controller = new AboutController(aboutActions, responseFactory, contentConverter);
        ResponseEntity<String> response = controller.about();

        ResponseEntity<String> expectedResponse = responseFactory.createOkContentResponse(contentConverter.getJsonString(model));
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testGetAboutData() {
        String version = "1.2.3";
        String created = "date";
        String description = "description";
        String gitHubUrl = "https://www.google.com";
        boolean initialized = true;
        String startupTime = "startup time is now";
        List<String> providers = List.of("provider_key");
        List<String> channels = List.of("channel_key");

        Gson gson = new Gson();
        ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        ResponseFactory responseFactory = new ResponseFactory();

        AboutModel model = new AboutModel(version, created, description, gitHubUrl, initialized, startupTime, providers, channels);
        AboutActions aboutActions = Mockito.mock(AboutActions.class);
        AboutController aboutController = new AboutController(aboutActions, responseFactory, contentConverter);

        Mockito.when(aboutActions.getAboutModel()).thenReturn(Optional.of(model));

        ResponseEntity<String> response = aboutController.about();
        ResponseEntity<String> expectedResponse = responseFactory.createOkContentResponse(contentConverter.getJsonString(model));
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testGetAboutDataNotPresent() {
        Gson gson = new Gson();
        ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());

        ResponseFactory responseFactory = new ResponseFactory();
        AboutActions aboutActions = Mockito.mock(AboutActions.class);
        AboutController aboutController = new AboutController(aboutActions, responseFactory, contentConverter);

        Mockito.when(aboutActions.getAboutModel()).thenReturn(Optional.empty());

        ResponseEntity<String> response = aboutController.about();
        ResponseEntity<String> expectedResponse = responseFactory.createMessageResponse(HttpStatus.NOT_FOUND, AboutController.ERROR_ABOUT_MODEL_NOT_FOUND);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }
}
