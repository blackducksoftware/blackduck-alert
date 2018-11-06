package com.synopsys.integration.alert.web.controller.handler;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.database.system.SystemMessage;
import com.synopsys.integration.alert.web.actions.AboutActions;
import com.synopsys.integration.alert.web.model.AboutModel;

public class AboutHandlerTest {

    @Test
    public void testGetAboutData() {
        final String version = "1.2.3";
        final String description = "description";
        final String gitHubUrl = "https://www.google.com";
        final boolean initialized = true;
        final String startupTime = "startup time is now";
        final List<SystemMessage> systemMessages = Collections.singletonList(new SystemMessage(new Date(), "ERROR", "startup messages"));

        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());

        final AboutModel model = new AboutModel(version, description, gitHubUrl, initialized, startupTime, systemMessages);
        final AboutActions aboutActions = Mockito.mock(AboutActions.class);
        final AboutHandler aboutHandler = new AboutHandler(contentConverter, aboutActions);

        Mockito.when(aboutActions.getAboutModel()).thenReturn(Optional.of(model));

        final ResponseEntity<String> response = aboutHandler.getAboutData();
        final String expectedJson = gson.toJson(model);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedJson, response.getBody());
    }

    @Test
    public void testGetAboutDataNotPresent() {
        final Gson gson = new Gson();
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());

        final AboutActions aboutActions = Mockito.mock(AboutActions.class);
        final AboutHandler aboutHandler = new AboutHandler(contentConverter, aboutActions);

        Mockito.when(aboutActions.getAboutModel()).thenReturn(Optional.empty());

        final ResponseEntity<String> response = aboutHandler.getAboutData();
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(AboutHandler.ERROR_ABOUT_MODEL_NOT_FOUND, response.getBody());
    }
}
