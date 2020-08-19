package com.synopsys.integration.alert.web.api.about;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;

public class AboutControllerTest {
    @Test
    public void testGetAboutData() {
        String version = "1.2.3";
        String created = "date";
        String description = "description";
        String aUrl = "https://www.google.com";
        boolean initialized = true;
        String startupTime = "startup time is now";
        DescriptorMetadata providerMetadata = Mockito.mock(DescriptorMetadata.class);
        DescriptorMetadata channelMetadata = Mockito.mock(DescriptorMetadata.class);
        Set<DescriptorMetadata> providers = Set.of(providerMetadata);
        Set<DescriptorMetadata> channels = Set.of(channelMetadata);

        AboutModel model = new AboutModel(version, created, description, aUrl, aUrl, initialized, startupTime, providers, channels);
        AboutActions aboutActions = Mockito.mock(AboutActions.class);
        AboutController aboutController = new AboutController(aboutActions);

        Mockito.when(aboutActions.getAboutModel()).thenReturn(Optional.of(model));

        try {
            AboutModel responseModel = aboutController.getAbout();
            assertNotNull("Expected a valid AboutModel as a response", responseModel);
        } catch (ResponseStatusException e) {
            fail(String.format("Failed to retrieve a valid AboutModel. Status Code: %d. Error Message: %s", e.getStatus().value(), e.getMessage()));
        }
    }

    @Test
    public void testGetAboutDataNotPresent() {
        AboutActions aboutActions = Mockito.mock(AboutActions.class);
        AboutController aboutController = new AboutController(aboutActions);

        Mockito.when(aboutActions.getAboutModel()).thenReturn(Optional.empty());

        String failureMessage = "Expected a ResponseStatusException to be thrown";
        try {
            aboutController.getAbout();
            fail(failureMessage);
        } catch (ResponseStatusException e) {
            assertEquals(HttpStatus.NOT_FOUND, e.getStatus());
            assertEquals(AboutController.ERROR_ABOUT_MODEL_NOT_FOUND, e.getReason());
        } catch (Exception e) {
            fail(failureMessage);
        }
    }

}
