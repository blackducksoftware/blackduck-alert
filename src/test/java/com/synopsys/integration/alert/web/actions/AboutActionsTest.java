package com.synopsys.integration.alert.web.actions;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.synopsys.integration.alert.common.AboutReader;
import com.synopsys.integration.alert.database.system.SystemMessage;
import com.synopsys.integration.alert.web.model.AboutModel;

public class AboutActionsTest {

    @Test
    public void testGetAboutModel() {
        final String version = "1.2.3";
        final String description = "description";
        final String gitHubUrl = "https://www.google.com";
        final boolean initialized = true;
        final String startupTime = "startup time is now";
        final List<SystemMessage> systemMessages = Collections.singletonList(new SystemMessage(new Date(), "ERROR", "startup messages"));

        final AboutModel model = new AboutModel(version, description, gitHubUrl, initialized, startupTime, systemMessages);
        final AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getAboutModel()).thenReturn(model);
        final AboutActions aboutActions = new AboutActions(aboutReader);
        final AboutModel resultModel = aboutActions.getAboutModel().get();

        assertEquals(model, resultModel);
    }
}
