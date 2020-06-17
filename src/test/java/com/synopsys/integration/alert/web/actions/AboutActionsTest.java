package com.synopsys.integration.alert.web.actions;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.AboutReader;
import com.synopsys.integration.alert.web.model.AboutModel;

public class AboutActionsTest {

    @Test
    public void testGetAboutModel() {
        String version = "1.2.3";
        String created = "date";
        String description = "description";
        String gitHubUrl = "https://www.google.com";
        boolean initialized = true;
        String startupTime = "startup time is now";
        List<String> providers = List.of("provider_key");
        List<String> channels = List.of("channel_key");

        AboutModel model = new AboutModel(version, created, description, gitHubUrl, initialized, startupTime, providers, channels);
        AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getAboutModel()).thenReturn(model);
        AboutActions aboutActions = new AboutActions(aboutReader);
        AboutModel resultModel = aboutActions.getAboutModel().get();

        assertEquals(model, resultModel);
    }
}
