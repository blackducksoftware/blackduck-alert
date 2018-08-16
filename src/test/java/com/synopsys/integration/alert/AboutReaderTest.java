package com.synopsys.integration.alert;

import static org.junit.Assert.*;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AboutReader;
import com.synopsys.integration.alert.web.model.AboutModel;

public class AboutReaderTest {

    @Test
    public void testAboutReadNull() {
        final AboutReader reader = new AboutReader(null);
        final AboutModel aboutModel = reader.getAboutModel();
        assertNull(aboutModel);
    }

    @Test
    public void testAboutRead() {
        final AboutReader reader = new AboutReader(new Gson());
        final AboutModel aboutModel = reader.getAboutModel();
        assertNotNull(aboutModel);
    }

    @Test
    public void testAboutReadVersionUnknown() {
        final AboutReader reader = new AboutReader(null);
        final String version = reader.getProductVersion();
        assertEquals(AboutReader.PRODUCT_VERSION_UNKNOWN, version);
    }

    @Test
    public void testAboutReadVersion() {
        final AboutReader reader = new AboutReader(new Gson());
        final String version = reader.getProductVersion();
        assertTrue(StringUtils.isNotBlank(version));
    }
}
