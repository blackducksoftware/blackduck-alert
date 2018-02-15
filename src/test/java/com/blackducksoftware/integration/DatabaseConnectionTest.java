package com.blackducksoftware.integration;

import com.blackducksoftware.integration.test.annotation.IntegrationTest;

/**
 * Marker interface for classifying JUnit tests that require database setup to run correctly.
 */
public interface DatabaseConnectionTest extends IntegrationTest {

}
