package com.blackducksoftware.integration.alert.web.actions;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.alert.common.AboutReader;
import com.blackducksoftware.integration.alert.web.model.AboutModel;

public class AboutActionsTest {

    @Test
    public void testGetAboutModel() {
        final String version = "1.2.3";
        final String description = "description";
        final String gitHubUrl = "https://www.google.com";

        final AboutModel model = new AboutModel(version, description, gitHubUrl);
        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getAboutModel()).thenReturn(model);
        final AboutActions aboutActions = new AboutActions(aboutReader);
        final AboutModel resultModel = aboutActions.getAboutModel().get();

        assertEquals(model, resultModel);
    }
}
