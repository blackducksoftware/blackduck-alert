package com.synopsys.integration.alert.web.controller;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.actions.AboutActions;
import com.synopsys.integration.alert.web.model.AboutModel;

public class AboutControllerTest {
    private final Gson gson = new Gson();
    ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());

    @Test
    public void testController() {
        final String version = "1.2.3";
        final String description = "description";
        final String gitHubUrl = "https://www.google.com";
        final boolean initialized = true;
        final String startupTime = "startup time is now";

        final ResponseFactory responseFactory = new ResponseFactory();
        final AboutModel model = new AboutModel(version, description, gitHubUrl, initialized, startupTime);
        final AboutActions aboutActions = Mockito.mock(AboutActions.class);

        Mockito.when(aboutActions.getAboutModel()).thenReturn(Optional.of(model));
        final AboutController controller = new AboutController(aboutActions, responseFactory, contentConverter);
        final ResponseEntity<String> response = controller.about();

        final ResponseEntity<String> expectedResponse = responseFactory.createOkContentResponse(contentConverter.getJsonString(model));
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testGetAboutData() {
        final String version = "1.2.3";
        final String description = "description";
        final String gitHubUrl = "https://www.google.com";
        final boolean initialized = true;
        final String startupTime = "startup time is now";

        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final ResponseFactory responseFactory = new ResponseFactory();

        final AboutModel model = new AboutModel(version, description, gitHubUrl, initialized, startupTime);
        final AboutActions aboutActions = Mockito.mock(AboutActions.class);
        final AboutController aboutController = new AboutController(aboutActions, responseFactory, contentConverter);

        Mockito.when(aboutActions.getAboutModel()).thenReturn(Optional.of(model));

        final ResponseEntity<String> response = aboutController.about();
        final ResponseEntity<String> expectedResponse = responseFactory.createOkContentResponse(contentConverter.getJsonString(model));
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }

    @Test
    public void testGetAboutDataNotPresent() {
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());

        final ResponseFactory responseFactory = new ResponseFactory();
        final AboutActions aboutActions = Mockito.mock(AboutActions.class);
        final AboutController aboutController = new AboutController(aboutActions, responseFactory, contentConverter);

        Mockito.when(aboutActions.getAboutModel()).thenReturn(Optional.empty());

        final ResponseEntity<String> response = aboutController.about();
        final ResponseEntity<String> expectedResponse = responseFactory.createMessageResponse(HttpStatus.NOT_FOUND, AboutController.ERROR_ABOUT_MODEL_NOT_FOUND);
        assertEquals(expectedResponse.getStatusCode(), response.getStatusCode());
        assertEquals(expectedResponse.getBody(), response.getBody());
    }
}
