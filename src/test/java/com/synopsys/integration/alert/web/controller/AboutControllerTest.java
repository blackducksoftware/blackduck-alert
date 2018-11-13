package com.synopsys.integration.alert.web.controller;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.actions.AboutActions;
import com.synopsys.integration.alert.web.controller.handler.AboutHandler;
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

        final AboutModel model = new AboutModel(version, description, gitHubUrl, initialized, startupTime);
        final AboutActions aboutActions = Mockito.mock(AboutActions.class);
        final AboutHandler aboutHandler = new AboutHandler(contentConverter, aboutActions);

        Mockito.when(aboutActions.getAboutModel()).thenReturn(Optional.of(model));
        final AboutController controller = new AboutController(aboutHandler);
        final ResponseEntity<String> response = controller.about();

        final String expectedJson = gson.toJson(model);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }
}
