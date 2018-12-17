package com.synopsys.integration.alert;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

public class TestPropertiesFileGenerator {
    @Test
    @Ignore
    public void generatePropertiesFile() throws IOException {
        final String propertiesFileName = ResourceLoader.RESOURCE_DIR + "/" + ResourceLoader.DEFAULT_PROPERTIES_FILE_LOCATION;
        System.out.println("Generating file: " + propertiesFileName + "..");

        final File testPropertiesFile = new File(propertiesFileName);
        if (!testPropertiesFile.exists()) {
            final boolean successfullyCreated = testPropertiesFile.createNewFile();
            if (!successfullyCreated) {
                System.out.println("There was a problem creating the file '" + propertiesFileName + "'.");
                return;
            }

            final StringBuilder dataBuilder = new StringBuilder();
            for (final TestPropertyKey propertyKey : TestPropertyKey.values()) {
                dataBuilder.append(propertyKey.getPropertyKey());
                dataBuilder.append('=');
                dataBuilder.append(System.lineSeparator());
            }
            FileUtils.write(testPropertiesFile, dataBuilder.toString(), Charset.defaultCharset(), false);
        } else {
            System.out.println("The file '" + propertiesFileName + "' already exists, please rename or back it up.");
        }
    }
}
