package com.synopsys.integration.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;
import com.synopsys.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultSystemStatusUtility;
import com.synopsys.integration.alert.database.system.DefaultSystemMessageUtility;
import com.synopsys.integration.alert.web.actions.DescriptorMetadataActions;
import com.synopsys.integration.alert.web.model.AboutModel;
import com.synopsys.integration.rest.RestConstants;

public class AboutReaderTest {
    private AlertProperties alertProperties;
    private DefaultSystemStatusUtility defaultSystemStatusUtility;
    private DefaultSystemMessageUtility defaultSystemMessageUtility;
    private DescriptorMetadataActions descriptorMetadataActions;

    @BeforeEach
    public void initialize() {
        alertProperties = Mockito.mock(AlertProperties.class);
        Mockito.when(alertProperties.getServerUrl()).thenReturn(Optional.empty());

        defaultSystemStatusUtility = Mockito.mock(DefaultSystemStatusUtility.class);
        Mockito.when(defaultSystemStatusUtility.isSystemInitialized()).thenReturn(Boolean.TRUE);
        Mockito.when(defaultSystemStatusUtility.getStartupTime()).thenReturn(DateUtils.createCurrentDateTimestamp());

        defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        Mockito.when(defaultSystemMessageUtility.getSystemMessages()).thenReturn(Collections.singletonList(new SystemMessageModel("1", RestConstants.formatDate(new Date()), "ERROR", "startup errors", "type")));

        descriptorMetadataActions = Mockito.mock(DescriptorMetadataActions.class);
        Mockito.when(descriptorMetadataActions.getDescriptorsByType(Mockito.anyString()))
            .thenReturn(Set.of(Mockito.mock(DescriptorMetadata.class), Mockito.mock(DescriptorMetadata.class)));
    }

    @Test
    public void testAboutReadNull() {
        AboutReader reader = new AboutReader(null, alertProperties, defaultSystemStatusUtility, descriptorMetadataActions);
        AboutModel aboutModel = reader.getAboutModel();
        assertNull(aboutModel);
    }

    @Test
    public void testAboutRead() {
        AboutReader reader = new AboutReader(new Gson(), alertProperties, defaultSystemStatusUtility, descriptorMetadataActions);
        AboutModel aboutModel = reader.getAboutModel();
        assertNotNull(aboutModel);
    }

    @Test
    public void testAboutReadVersionUnknown() {
        AboutReader reader = new AboutReader(null, alertProperties, defaultSystemStatusUtility, descriptorMetadataActions);
        String version = reader.getProductVersion();
        assertEquals(AboutReader.PRODUCT_VERSION_UNKNOWN, version);
    }

    @Test
    public void testAboutReadVersion() {
        AboutReader reader = new AboutReader(new Gson(), alertProperties, defaultSystemStatusUtility, descriptorMetadataActions);
        String version = reader.getProductVersion();
        assertTrue(StringUtils.isNotBlank(version));
    }
}
