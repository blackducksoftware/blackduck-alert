package com.blackducksoftware.integration.alert.web.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.alert.common.ContentConverter;
import com.blackducksoftware.integration.alert.common.model.AboutModel;
import com.blackducksoftware.integration.alert.web.actions.AboutActions;
import com.blackducksoftware.integration.alert.web.controller.handler.AboutHandler;
import com.google.gson.Gson;

public class AboutControllerTest {
    private final Gson gson = new Gson();
    ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());

    @Test
    public void testController() {
        final String version = "1.2.3";
        final String description = "description";
        final String gitHubUrl = "https://www.google.com";

        final AboutModel model = new AboutModel(version, description, gitHubUrl);
        final AboutActions aboutActions = Mockito.mock(AboutActions.class);
        final AboutHandler aboutHandler = new AboutHandler(contentConverter, aboutActions);

        Mockito.when(aboutActions.getAboutModel()).thenReturn(model);
        final AboutController controller = new AboutController(aboutHandler);
        final ResponseEntity<String> response = controller.about();

        final String expectedJson = gson.toJson(model);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }
}
