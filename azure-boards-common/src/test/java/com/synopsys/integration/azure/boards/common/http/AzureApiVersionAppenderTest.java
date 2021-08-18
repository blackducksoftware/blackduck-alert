package com.synopsys.integration.azure.boards.common.http;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class AzureApiVersionAppenderTest {
    private static final String TEST_SPEC = "https://a-server:12345/an/api/route";

    @Test
    public void appendApiVersionTest() {
        String testVersion = "a-version";
        AzureApiVersionAppender apiVersionAppender = new AzureApiVersionAppender();

        String specWithVersion = apiVersionAppender.appendApiVersion(TEST_SPEC, testVersion);
        assertSpec(specWithVersion, testVersion);
    }

    @Test
    public void appendApiVersion_X_Y_Z_Test() {
        AzureApiVersionAppender apiVersionAppender = new AzureApiVersionAppender();
        String spec5_0 = apiVersionAppender.appendApiVersion5_0(TEST_SPEC);
        assertSpec(spec5_0, AzureApiVersionAppender.AZURE_API_VERSION_5_0);

        String spec5_1 = apiVersionAppender.appendApiVersion5_1(TEST_SPEC);
        assertSpec(spec5_1, AzureApiVersionAppender.AZURE_API_VERSION_5_1);

        String spec5_1_Preview_1 = apiVersionAppender.appendApiVersion5_1_Preview_1(TEST_SPEC);
        assertSpec(spec5_1_Preview_1, AzureApiVersionAppender.AZURE_API_VERSION_5_1_PREVIEW_1);

        String spec5_1_Preview_2 = apiVersionAppender.appendApiVersion5_1_Preview_2(TEST_SPEC);
        assertSpec(spec5_1_Preview_2, AzureApiVersionAppender.AZURE_API_VERSION_5_1_PREVIEW_2);

        String spec5_1_Preview_3 = apiVersionAppender.appendApiVersion5_1_Preview_3(TEST_SPEC);
        assertSpec(spec5_1_Preview_3, AzureApiVersionAppender.AZURE_API_VERSION_5_1_PREVIEW_3);
    }

    private static void assertSpec(String specWithVersion, String expectedVersion) {
        assertNotNull(specWithVersion, "Expected the returned String to not be null");
        assertTrue(specWithVersion.startsWith(TEST_SPEC), "Expected the returned String to start with the original spec");
        assertTrue(specWithVersion.endsWith(expectedVersion), "Expected the returned String to end with the specified version");
    }

}
