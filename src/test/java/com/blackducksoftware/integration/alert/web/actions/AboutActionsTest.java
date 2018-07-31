package com.blackducksoftware.integration.alert.web.actions;

import static org.junit.Assert.assertEquals;

import java.util.Optional;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.provider.blackduck.BlackDuckProperties;
import com.blackducksoftware.integration.alert.web.model.AboutModel;

public class AboutActionsTest {

    @Test
    public void testGetAboutModel() {
        final String version = "1.2.3";
        final String description = "description";
        final String gitHubUrl = "https://www.google.com";

        final AboutModel model = new AboutModel(version, description, gitHubUrl);
        final BlackDuckProperties hubProperties = Mockito.mock(BlackDuckProperties.class);
        Mockito.when(hubProperties.getAboutModel()).thenReturn(Optional.of(model));
        final AboutActions aboutActions = new AboutActions(hubProperties);
        final AboutModel resultModel = aboutActions.getAboutModel().get();

        assertEquals(model, resultModel);
    }
}
