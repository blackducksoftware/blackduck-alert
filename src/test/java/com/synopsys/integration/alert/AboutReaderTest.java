package com.synopsys.integration.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.email.EmailChannelKey;
import com.synopsys.integration.alert.common.descriptor.ChannelDescriptor;
import com.synopsys.integration.alert.common.descriptor.DescriptorKey;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.common.descriptor.ProviderDescriptor;
import com.synopsys.integration.alert.common.persistence.model.SystemMessageModel;
import com.synopsys.integration.alert.common.util.DateUtils;
import com.synopsys.integration.alert.database.api.DefaultSystemStatusUtility;
import com.synopsys.integration.alert.database.system.DefaultSystemMessageUtility;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProviderKey;
import com.synopsys.integration.alert.web.model.AboutModel;
import com.synopsys.integration.rest.RestConstants;

public class AboutReaderTest {
    private DefaultSystemStatusUtility defaultSystemStatusUtility;
    private DefaultSystemMessageUtility defaultSystemMessageUtility;
    private DescriptorMap descriptorMap;
    private DescriptorKey providerKey = new BlackDuckProviderKey();
    private DescriptorKey channelKey = new EmailChannelKey();

    @BeforeEach
    public void initialize() {
        defaultSystemStatusUtility = Mockito.mock(DefaultSystemStatusUtility.class);
        Mockito.when(defaultSystemStatusUtility.isSystemInitialized()).thenReturn(Boolean.TRUE);
        Mockito.when(defaultSystemStatusUtility.getStartupTime()).thenReturn(DateUtils.createCurrentDateTimestamp());
        defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageUtility.class);
        Mockito.when(defaultSystemMessageUtility.getSystemMessages()).thenReturn(Collections.singletonList(new SystemMessageModel("1", RestConstants.formatDate(new Date()), "ERROR", "startup errors", "type")));
        descriptorMap = Mockito.mock(DescriptorMap.class);
        Mockito.when(descriptorMap.getProviderDescriptorMap()).thenReturn(Map.of(providerKey, Mockito.mock(ProviderDescriptor.class)));
        Mockito.when(descriptorMap.getChannelDescriptorMap()).thenReturn(Map.of(channelKey, Mockito.mock(ChannelDescriptor.class)));
    }

    @Test
    public void testAboutReadNull() {
        AboutReader reader = new AboutReader(null, defaultSystemStatusUtility, descriptorMap);
        AboutModel aboutModel = reader.getAboutModel();
        assertNull(aboutModel);
    }

    @Test
    public void testAboutRead() {
        AboutReader reader = new AboutReader(new Gson(), defaultSystemStatusUtility, descriptorMap);
        AboutModel aboutModel = reader.getAboutModel();
        assertNotNull(aboutModel);
    }

    @Test
    public void testAboutReadVersionUnknown() {
        AboutReader reader = new AboutReader(null, defaultSystemStatusUtility, descriptorMap);
        String version = reader.getProductVersion();
        assertEquals(AboutReader.PRODUCT_VERSION_UNKNOWN, version);
    }

    @Test
    public void testAboutReadVersion() {
        AboutReader reader = new AboutReader(new Gson(), defaultSystemStatusUtility, descriptorMap);
        String version = reader.getProductVersion();
        assertTrue(StringUtils.isNotBlank(version));
    }
}
