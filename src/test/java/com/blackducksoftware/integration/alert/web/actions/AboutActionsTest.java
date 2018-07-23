package com.blackducksoftware.integration.alert.web.actions;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.common.model.AboutModel;
import com.blackducksoftware.integration.alert.config.GlobalProperties;

public class AboutActionsTest {

    @Test
    public void testGetAboutModel() {
        final String version = "1.2.3";
        final String description = "description";
        final String gitHubUrl = "https://www.google.com";

        final AboutModel model = new AboutModel(version, description, gitHubUrl);
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        Mockito.when(globalProperties.getAboutModel()).thenReturn(Optional.of(model));
        final AboutActions aboutActions = new AboutActions(globalProperties);
        final AboutModel resultModel = aboutActions.getAboutModel().get();

        assertEquals(model, resultModel);
    }
}
