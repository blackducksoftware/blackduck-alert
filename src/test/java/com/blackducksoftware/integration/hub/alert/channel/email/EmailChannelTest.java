package com.blackducksoftware.integration.hub.alert.channel.email;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.digest.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryDataBuilder;
import com.blackducksoftware.integration.hub.alert.digest.model.ItemData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectDataBuilder;
import com.blackducksoftware.integration.hub.notification.processor.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.google.gson.Gson;

public class EmailChannelTest {

    // TODO fix NPE
    // @Test
    public void sendEmailTest() throws Exception {
        final List<VulnerabilityEntity> vulns = new ArrayList<>();
        final VulnerabilityEntity vulnerability = new VulnerabilityEntity("Vuln ID", "Vuln Operation");
        vulns.add(vulnerability);

        final HashMap<String, Object> itemModel = new HashMap<>();
        itemModel.put(ItemTypeEnum.COMPONENT.toString(), "Manual Test Component");
        itemModel.put(ItemTypeEnum.VERSION.toString(), "1.0.3");
        itemModel.put(ItemTypeEnum.RULE.toString(), "Manual Policy Rule");
        final ItemData data = new ItemData(itemModel);
        final CategoryDataBuilder categoryBuilder = new CategoryDataBuilder();
        categoryBuilder.addItem(data);
        categoryBuilder.incrementItemCount(1);
        categoryBuilder.setCategoryKey(NotificationCategoryEnum.POLICY_VIOLATION.toString());

        final ProjectDataBuilder projectDataBuilder = new ProjectDataBuilder();
        projectDataBuilder.setProjectName("Manual Test Project");
        projectDataBuilder.setProjectVersion("Manual Test Project Version");
        projectDataBuilder.setDigestType(DigestTypeEnum.REAL_TIME);
        projectDataBuilder.addCategoryBuilder(NotificationCategoryEnum.POLICY_VIOLATION, categoryBuilder);
        final ProjectData projectData = projectDataBuilder.build();

        final Properties testProperties = new Properties();
        testProperties.load(new FileInputStream(new File("./src/test/resources/application.properties")));

        final TestGlobalProperties globalProperties = new TestGlobalProperties(null);
        globalProperties.setHubUrl(testProperties.getProperty("blackduck.hub.url"));
        final Gson gson = new Gson();
        final EmailChannel emailChannel = new EmailChannel(globalProperties, gson, null);
        final EmailEvent event = new EmailEvent(projectData);

        final EmailConfigEntity emailConfigEntity = new EmailConfigEntity(testProperties.getProperty("mail.smtp.host"), null, null, null, null, null, testProperties.getProperty("mail.smtp.from"), null, null, null, null, null, null, null,
                testProperties.getProperty("hub.email.template.directory"), testProperties.getProperty("logo.image"), "Test Subject Line");

        emailChannel.sendMessage(event, emailConfigEntity);
    }

}
