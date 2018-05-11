package com.blackducksoftware.integration.hub.alert.web.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.hub.alert.model.AboutModel;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.actions.AboutActions;
import com.blackducksoftware.integration.hub.alert.web.controller.handler.AboutHandler;
import com.google.gson.Gson;

public class AboutControllerTest {
    private final ObjectTransformer objectTransformer = new ObjectTransformer();
    private final Gson gson = new Gson();

    @Test
    public void testController() {
        final String version = "1.2.3";
        final String description = "description";
        final String gitHubUrl = "https://www.google.com";

        final AboutModel model = new AboutModel(version, description, gitHubUrl);
        final AboutActions aboutActions = Mockito.mock(AboutActions.class);
        final AboutHandler aboutHandler = new AboutHandler(objectTransformer, gson, aboutActions);

        Mockito.when(aboutActions.getAboutModel()).thenReturn(model);
        final AboutController controller = new AboutController(aboutHandler);
        final ResponseEntity<String> response = controller.about();

        final String expectedJson = gson.toJson(model);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }
}
