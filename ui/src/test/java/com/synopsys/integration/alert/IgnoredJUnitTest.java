package com.synopsys.integration.alert;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class IgnoredJUnitTest {
    @Test
    @Disabled
    public void testIgnoredJUnitTestForUI() {
        // TODO look at the Jenkins build and see if there is a better way to define the regular expression for the test-results to exclude sub-modules.
        /*
            This is to work around an issue with the Jenkins Pipeline JUnit stage.
            The JUnit stage is looking for JUnit test result XML files in a sub-modules build/test-results directory.
            This sub-module only has javascript code and the build fails in the JUnit stage because the JUnit XML file doesn't exist.
            Added this test to force the ui sub-module build to generate a JUnit XML test results file to avoid a break in the Jenkins build.
         */
    }
}
