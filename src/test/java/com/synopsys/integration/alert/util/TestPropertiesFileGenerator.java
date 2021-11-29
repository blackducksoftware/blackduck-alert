package com.synopsys.integration.alert.util;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.test.common.TestPropertyKey;
import com.synopsys.integration.alert.test.common.TestResourceUtils;

public class TestPropertiesFileGenerator {
    @Test
    @Disabled("This test is to generate the test.properties for new developers.")
    public void generatePropertiesFile() throws IOException {
        Path propertiesFilePath = TestResourceUtils.createTestPropertiesCanonicalFilePath();
        System.out.println("Generating file: " + propertiesFilePath + "..");

        File testPropertiesFile = propertiesFilePath.toFile();
        if (!testPropertiesFile.exists()) {
            boolean successfullyCreated = testPropertiesFile.createNewFile();
            if (!successfullyCreated) {
                System.out.println("There was a problem creating the file '" + propertiesFilePath + "'.");
                return;
            }

            StringBuilder dataBuilder = new StringBuilder();
            for (TestPropertyKey propertyKey : TestPropertyKey.values()) {
                dataBuilder.append(propertyKey.getPropertyKey());
                dataBuilder.append('=');
                dataBuilder.append(System.lineSeparator());
            }
            FileUtils.write(testPropertiesFile, dataBuilder.toString(), Charset.defaultCharset(), false);
        } else {
            System.out.println("The file '" + propertiesFilePath + "' already exists, please rename or back it up.");
        }
    }

}
