package com.synopsys.integration.alert;

import static org.junit.Assert.*;

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AboutReader;
import com.synopsys.integration.alert.common.descriptor.DescriptorMap;
import com.synopsys.integration.alert.web.model.AboutModel;

public class AboutReaderTest {

    @Test
    public void testAboutReadNull() {
        final DescriptorMap descriptorMap = Mockito.mock(DescriptorMap.class);
        Mockito.when(descriptorMap.getChannelDescriptorMap()).thenReturn(Collections.emptyMap());
        Mockito.when(descriptorMap.getProviderDescriptorMap()).thenReturn(Collections.emptyMap());
        final AboutReader reader = new AboutReader(null, descriptorMap);
        final AboutModel aboutModel = reader.getAboutModel();
        assertNull(aboutModel);
    }

    @Test
    public void testAboutRead() {
        final DescriptorMap descriptorMap = Mockito.mock(DescriptorMap.class);
        Mockito.when(descriptorMap.getChannelDescriptorMap()).thenReturn(Collections.emptyMap());
        Mockito.when(descriptorMap.getProviderDescriptorMap()).thenReturn(Collections.emptyMap());
        final AboutReader reader = new AboutReader(new Gson(), descriptorMap);
        final AboutModel aboutModel = reader.getAboutModel();
        assertNotNull(aboutModel);
    }

    @Test
    public void testAboutReadVersionUnknown() {
        final DescriptorMap descriptorMap = Mockito.mock(DescriptorMap.class);
        Mockito.when(descriptorMap.getChannelDescriptorMap()).thenReturn(Collections.emptyMap());
        Mockito.when(descriptorMap.getProviderDescriptorMap()).thenReturn(Collections.emptyMap());
        final AboutReader reader = new AboutReader(null, descriptorMap);
        final String version = reader.getProductVersion();
        assertEquals(AboutReader.PRODUCT_VERSION_UNKNOWN, version);
    }

    @Test
    public void testAboutReadVersion() {
        final DescriptorMap descriptorMap = Mockito.mock(DescriptorMap.class);
        Mockito.when(descriptorMap.getChannelDescriptorMap()).thenReturn(Collections.emptyMap());
        Mockito.when(descriptorMap.getProviderDescriptorMap()).thenReturn(Collections.emptyMap());
        final AboutReader reader = new AboutReader(new Gson(), descriptorMap);
        final String version = reader.getProductVersion();
        assertTrue(StringUtils.isNotBlank(version));
    }
}
