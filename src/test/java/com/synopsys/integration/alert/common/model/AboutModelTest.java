package com.synopsys.integration.alert.common.model;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.List;

import org.junit.Test;

import com.synopsys.integration.alert.web.model.AboutModel;

public class AboutModelTest {

    @Test
    public void testWithValues() {
        final String version = "1.2.3";
        final String description = "description";
        final String gitHubUrl = "https://www.google.com";
        final boolean initialized = true;
        final String startupTime = "startup time is now";
        final List<String> startupErrors = Collections.singletonList("startup errors");

        final AboutModel model = new AboutModel(version, description, gitHubUrl, initialized, startupTime, startupErrors);

        assertEquals(version, model.getVersion());
        assertEquals(description, model.getDescription());
        assertEquals(gitHubUrl, model.getProjectUrl());
        assertEquals(initialized, model.isInitialized());
        assertEquals(startupTime, model.getStartupTime());
        assertEquals(startupErrors, model.getStartupErrors());
    }
}
