package com.synopsys.integration.alert.channel.email.attachment;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.message.model.LinkableItem;
import com.synopsys.integration.alert.api.processor.extract.model.ProviderDetails;
import com.synopsys.integration.alert.api.processor.extract.model.project.ProjectMessage;
import com.synopsys.integration.alert.api.processor.extract.model.project.ProjectOperation;
import com.synopsys.integration.alert.test.common.MockAlertProperties;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;

class EmailAttachmentFileCreatorTest {
    private static MockAlertProperties alertProperties;
    private final MessageContentGroupCsvCreator messageContentGroupCsvCreator = new MessageContentGroupCsvCreator();
    private final Gson gson = BlackDuckServicesFactory.createDefaultGson();

    private final String providerTestString = "Provider Test Message";
    private final String projectTestString = "Common Project Test Message";
    private ProjectMessage projectMessage;

    @BeforeEach
    void init() {
        alertProperties = new MockAlertProperties();
        alertProperties.setAlertEmailAttachmentsDir("/tmp");

        LinkableItem provider = new LinkableItem("Provider", providerTestString);
        ProviderDetails providerDetails = new ProviderDetails(1L, provider);
        projectMessage = ProjectMessage.projectStatusInfo(providerDetails, new LinkableItem("Project", projectTestString), ProjectOperation.CREATE);
    }

    @Test
    void testCreateCsv() {
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(alertProperties, messageContentGroupCsvCreator, gson);
        Optional<File> csvFile = emailAttachmentFileCreator.createAttachmentFile(EmailAttachmentFormat.CSV, projectMessage);

        assertTrue(csvFile.isPresent());
        String csvContent = assertDoesNotThrow(() -> FileUtils.readFileToString(csvFile.get(), Charset.defaultCharset()));
        assertTrue(csvContent.contains(providerTestString));
        assertTrue(csvContent.contains(projectTestString));
        assertDoesNotThrow(() -> emailAttachmentFileCreator.cleanUpAttachmentFile(csvFile.get()));
    }

    @Test
    void testCreateJson() {
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(alertProperties, messageContentGroupCsvCreator, gson);
        Optional<File> jsonFile = emailAttachmentFileCreator.createAttachmentFile(EmailAttachmentFormat.JSON, projectMessage);

        assertTrue(jsonFile.isPresent());
        String jsonContent = assertDoesNotThrow(() -> FileUtils.readFileToString(jsonFile.get(), Charset.defaultCharset()));
        assertTrue(jsonContent.contains(providerTestString));
        assertTrue(jsonContent.contains(projectTestString));
        assertDoesNotThrow(() -> emailAttachmentFileCreator.cleanUpAttachmentFile(jsonFile.get()));
    }

    @Test
    void testCreateXml() {
        EmailAttachmentFileCreator emailAttachmentFileCreator = new EmailAttachmentFileCreator(alertProperties, messageContentGroupCsvCreator, gson);
        Optional<File> xmlFile = emailAttachmentFileCreator.createAttachmentFile(EmailAttachmentFormat.XML, projectMessage);

        assertTrue(xmlFile.isPresent());
        String xmlContent = assertDoesNotThrow(() -> FileUtils.readFileToString(xmlFile.get(), Charset.defaultCharset()));
        assertTrue(xmlContent.contains(providerTestString));
        assertTrue(xmlContent.contains(projectTestString));
        assertDoesNotThrow(() -> emailAttachmentFileCreator.cleanUpAttachmentFile(xmlFile.get()));
    }
}
