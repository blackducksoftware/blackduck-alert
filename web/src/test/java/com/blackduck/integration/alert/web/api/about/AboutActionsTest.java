package com.blackduck.integration.alert.web.api.about;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.blackduck.integration.alert.common.persistence.accessor.SystemStatusAccessor;
import com.blackduck.integration.alert.common.rest.AlertWebServerUrlManager;
import com.blackduck.integration.alert.web.api.metadata.DescriptorMetadataActions;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

public class AboutActionsTest {

    @Test
    public void testGetAboutModel() {
        String version = "1.2.3";
        String created = "date";
        String description = "description";
        String aUrl = "https://www.google.com";
        String commitHash = "abc123xyz";
        String copyrightYear = "year";
        boolean initialized = true;
        String startupTime = "startup time is now";
        DescriptorMetadata providerMetadata = Mockito.mock(DescriptorMetadata.class);
        DescriptorMetadata channelMetadata = Mockito.mock(DescriptorMetadata.class);
        Set<DescriptorMetadata> providers = Set.of(providerMetadata);
        Set<DescriptorMetadata> channels = Set.of(channelMetadata);

        AboutModel model = new AboutModel(version, created, description, aUrl, commitHash, copyrightYear, aUrl, initialized, startupTime, providers, channels);
        AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getAboutModel()).thenReturn(Optional.of(model));
        AboutActions aboutActions = new AboutActions(aboutReader);
        ActionResponse<AboutModel> resultModel = aboutActions.getAboutModel();
        assertTrue(resultModel.isSuccessful());
        assertTrue(resultModel.hasContent());
        assertEquals(model, resultModel.getContent().orElse(null));
    }

    @Test
    public void testGetAboutDataNotPresent() {
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        AlertWebServerUrlManager alertWebServerUrlManager = Mockito.mock(AlertWebServerUrlManager.class);
        SystemStatusAccessor systemStatusAccessor = Mockito.mock(SystemStatusAccessor.class);
        DescriptorMetadataActions descriptorActions = Mockito.mock(DescriptorMetadataActions.class);

        AboutReader aboutReader = new AboutReader(gson, alertWebServerUrlManager, systemStatusAccessor, descriptorActions);
        AboutActions aboutActions = new AboutActions(aboutReader);
        ActionResponse<AboutModel> resultModel = aboutActions.getAboutModel();
        assertTrue(resultModel.isError());
        assertFalse(resultModel.hasContent());
    }
}
