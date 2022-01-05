/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.AlertProperties;

@Component
public final class FilePersistenceUtil {
    private final File parentDataDirectory;
    private final File secretsDirectory;
    private final File uploadsDirectory;
    private final Gson gson;

    @Autowired
    public FilePersistenceUtil(final AlertProperties alertProperties, final Gson gson) {
        this.gson = gson;
        String dataDirectory = "data/";
        if (StringUtils.isNotBlank(alertProperties.getAlertConfigHome())) {
            dataDirectory = String.format("%s/data", alertProperties.getAlertConfigHome());
        }
        this.parentDataDirectory = new File(dataDirectory);
        this.secretsDirectory = new File(alertProperties.getAlertSecretsDir());
        this.uploadsDirectory = new File(parentDataDirectory, "uploads");
    }

    public void writeToFile(final String fileName, final String content) throws IOException {
        writeToFile(createFile(fileName), content.getBytes(StandardCharsets.UTF_8));
    }

    public void writeToFile(File destination, byte[] data) throws IOException {
        // destination is a path to a file.  Make sure all parent directories exist before writing.
        destination.getParentFile().mkdirs();
        Files.write(destination.toPath(), data, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    public void writeToFile(File destination, InputStream inputStream) throws IOException {
        // destination is a path to a file.  Make sure all parent directories exist before writing.
        destination.getParentFile().mkdirs();
        Files.copy(inputStream, destination.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    public void writeJsonToFile(final String fileName, final Object content) throws IOException {
        final String jsonString = gson.toJson(content);
        writeToFile(fileName, jsonString);
    }

    public void writeFileToUploadsDirectory(final String fileName, InputStream inputStream) throws IOException {
        File uploadFile = createUploadsFile(fileName);
        writeToFile(uploadFile, inputStream);
    }

    public String readFromFile(final String fileName) throws IOException {
        return readFromFile(parentDataDirectory, fileName);
    }

    public String readFromSecretsFile(final String secretsFileName) throws IOException {
        return readFromFile(secretsDirectory, secretsFileName);
    }

    private String readFromFile(final File parentDirectory, final String fileName) throws IOException {
        return Files.readString(createFile(parentDirectory, fileName).toPath(), StandardCharsets.UTF_8);
    }

    public <T> T readJsonFromFile(final String fileName, final Class<T> clazz) throws IOException {
        final String jsonString = readFromFile(fileName);
        return gson.fromJson(jsonString, clazz);
    }

    public boolean exists(final String fileName) {
        return exists(parentDataDirectory, fileName);
    }

    public boolean uploadFileExists(String fileName) {
        return exists(uploadsDirectory, fileName);
    }

    public boolean exists(final File parentDirectory, final String fileName) {
        final File file = createFile(parentDirectory, fileName);
        return file.exists();
    }

    public void delete(final String fileName) throws IOException {
        final File file = createFile(fileName);
        FileUtils.forceDelete(file);
    }

    public void delete(File file) throws IOException {
        FileUtils.forceDelete(file);
    }

    public File createUploadsFile(String fileName) {
        return new File(uploadsDirectory, fileName);
    }

    private File createFile(final String fileName) {
        return createFile(parentDataDirectory, fileName);
    }

    private File createFile(final File parent, final String fileName) {
        return new File(parent, fileName);
    }
}
