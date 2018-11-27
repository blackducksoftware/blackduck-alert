package com.synopsys.integration.alert;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AboutReader;
import com.synopsys.integration.alert.database.system.SystemMessage;
import com.synopsys.integration.alert.database.system.SystemMessageUtility;
import com.synopsys.integration.alert.database.system.SystemStatusUtility;
import com.synopsys.integration.alert.web.model.AboutModel;

public class AboutReaderTest {
    private SystemStatusUtility systemStatusUtility;
    private SystemMessageUtility systemMessageUtility;

    @Before
    public void initialize() {
        systemStatusUtility = Mockito.mock(SystemStatusUtility.class);
        Mockito.when(systemStatusUtility.isSystemInitialized()).thenReturn(Boolean.TRUE);
        Mockito.when(systemStatusUtility.getStartupTime()).thenReturn(new Date());
        systemMessageUtility = Mockito.mock(SystemMessageUtility.class);
        Mockito.when(systemMessageUtility.getSystemMessages()).thenReturn(Collections.singletonList(new SystemMessage(new Date(), "ERROR", "startup errors", "type")));
    }

    @Test
    public void testAboutReadNull() {
        final AboutReader reader = new AboutReader(null, systemStatusUtility);
        final AboutModel aboutModel = reader.getAboutModel();
        assertNull(aboutModel);
    }

    @Test
    public void testAboutRead() {
        final AboutReader reader = new AboutReader(new Gson(), systemStatusUtility);
        final AboutModel aboutModel = reader.getAboutModel();
        assertNotNull(aboutModel);
    }

    @Test
    public void testAboutReadVersionUnknown() {
        final AboutReader reader = new AboutReader(null, systemStatusUtility);
        final String version = reader.getProductVersion();
        assertEquals(AboutReader.PRODUCT_VERSION_UNKNOWN, version);
    }

    @Test
    public void testAboutReadVersion() {
        final AboutReader reader = new AboutReader(new Gson(), systemStatusUtility);
        final String version = reader.getProductVersion();
        assertTrue(StringUtils.isNotBlank(version));
    }
}
