package com.synopsys.integration.alert.web.api.about;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.persistence.accessor.SystemStatusAccessor;
import com.synopsys.integration.alert.web.api.metadata.DescriptorMetadataActions;

public class AboutActionsTest {

    @Test
    public void testGetAboutModel() {
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
        AboutReader aboutReader = Mockito.mock(AboutReader.class);
        Mockito.when(aboutReader.getAboutModel()).thenReturn(Optional.of(model));
        AboutActions aboutActions = new AboutActions(aboutReader);
        ActionResponse<AboutModel> resultModel = aboutActions.getAboutModel();
        Assertions.assertTrue(resultModel.isSuccessful());
        Assertions.assertTrue(resultModel.hasContent());
        Assertions.assertEquals(model, resultModel.getContent().orElse(null));
    }

    @Test
    public void testGetAboutDataNotPresent() {
        Gson gson = new Gson();
        AlertProperties alertProperties = Mockito.mock(AlertProperties.class);
        SystemStatusAccessor systemStatusAccessor = Mockito.mock(SystemStatusAccessor.class);
        DescriptorMetadataActions descriptorActions = Mockito.mock(DescriptorMetadataActions.class);

        Mockito.when(alertProperties.getServerUrl()).thenThrow(new NullPointerException("Exception for about test"));
        AboutReader aboutReader = new AboutReader(gson, alertProperties, systemStatusAccessor, descriptorActions);
        AboutActions aboutActions = new AboutActions(aboutReader);
        ActionResponse<AboutModel> resultModel = aboutActions.getAboutModel();
        Assertions.assertTrue(resultModel.isError());
        Assertions.assertFalse(resultModel.hasContent());
    }
}
