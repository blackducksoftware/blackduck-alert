package com.blackducksoftware.integration.alert.web.actions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.config.GlobalProperties;
import com.blackducksoftware.integration.alert.model.AboutModel;
import com.blackducksoftware.integration.alert.web.actions.AboutActions;

public class AboutActionsTest {

    @Test
    public void testGetAboutModel() {
        final String version = "1.2.3";
        final String description = "description";
        final String gitHubUrl = "https://www.google.com";

        final AboutModel model = new AboutModel(version, description, gitHubUrl);
        final GlobalProperties globalProperties = Mockito.mock(GlobalProperties.class);
        Mockito.when(globalProperties.getAboutModel()).thenReturn(model);
        final AboutActions aboutActions = new AboutActions(globalProperties);
        final AboutModel resultModel = aboutActions.getAboutModel();

        assertEquals(model, resultModel);
    }
}
