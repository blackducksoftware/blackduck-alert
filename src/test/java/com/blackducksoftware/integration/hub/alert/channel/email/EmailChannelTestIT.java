package com.blackducksoftware.integration.hub.alert.channel.email;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;

import com.blackducksoftware.integration.hub.alert.MockUtils;
import com.blackducksoftware.integration.hub.alert.TestGlobalProperties;
import com.blackducksoftware.integration.hub.alert.channel.RestChannelTest;
import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.GlobalConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.HubUsersEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.VulnerabilityEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.GlobalRepository;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.HubUsersRepository;
import com.blackducksoftware.integration.hub.alert.digest.DigestTypeEnum;
import com.blackducksoftware.integration.hub.alert.digest.model.CategoryDataBuilder;
import com.blackducksoftware.integration.hub.alert.digest.model.ItemData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectData;
import com.blackducksoftware.integration.hub.alert.digest.model.ProjectDataBuilder;
import com.blackducksoftware.integration.hub.notification.processor.ItemTypeEnum;
import com.blackducksoftware.integration.hub.notification.processor.NotificationCategoryEnum;
import com.google.gson.Gson;

public class EmailChannelTestIT extends RestChannelTest {
    private final MockUtils mockUtils = new MockUtils();

    @Test
    public void sendEmailTest() throws Exception {
        Assume.assumeTrue(properties.containsKey("mail.smtp.host"));
        Assume.assumeTrue(properties.containsKey("mail.smtp.from"));
        Assume.assumeTrue(properties.containsKey("hub.email.template.directory"));
        Assume.assumeTrue(properties.containsKey("logo.image"));

        Assume.assumeTrue(properties.containsKey("blackduck.hub.url"));
        Assume.assumeTrue(properties.containsKey("blackduck.hub.username"));
        Assume.assumeTrue(properties.containsKey("blackduck.hub.password"));

        final HubUsersRepository hubUsersRepository = Mockito.mock(HubUsersRepository.class);
        final HubUsersEntity userEntity = mockUtils.createHubUsersEntity(properties.getProperty("blackduck.hub.username"));
        Mockito.when(hubUsersRepository.findOne(Mockito.anyLong())).thenReturn(userEntity);

        final GlobalRepository globalRepository = Mockito.mock(GlobalRepository.class);
        final GlobalConfigEntity globalConfig = new GlobalConfigEntity(300, properties.getProperty("blackduck.hub.username"), properties.getProperty("blackduck.hub.password"), "", "", "");
        Mockito.when(globalRepository.findAll()).thenReturn(Arrays.asList(globalConfig));

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
        categoryBuilder.setCategoryKey(NotificationCategoryEnum.POLICY_VIOLATION.toString());

        final ProjectDataBuilder projectDataBuilder = new ProjectDataBuilder();
        projectDataBuilder.setProjectName("Manual Test Project");
        projectDataBuilder.setProjectVersion("Manual Test Project Version");
        projectDataBuilder.setDigestType(DigestTypeEnum.REAL_TIME);
        projectDataBuilder.addCategoryBuilder(NotificationCategoryEnum.POLICY_VIOLATION, categoryBuilder);
        final ProjectData projectData = projectDataBuilder.build();

        final TestGlobalProperties globalProperties = new TestGlobalProperties(globalRepository);
        globalProperties.hubUrl = properties.getProperty("blackduck.hub.url");

        final String trustCert = properties.getProperty("blackduck.hub.trust.cert");
        if (trustCert != null) {
            globalProperties.hubTrustCertificate = Boolean.valueOf(trustCert);
        }

        final Gson gson = new Gson();
        final EmailChannel emailChannel = new EmailChannel(globalProperties, gson, hubUsersRepository, null, null);
        final EmailEvent event = new EmailEvent(projectData, userEntity.getId());

        final EmailConfigEntity emailConfigEntity = new EmailConfigEntity(properties.getProperty("mail.smtp.host"), null, null, null, null, null, properties.getProperty("mail.smtp.from"), null, null, null, null, null, null, null,
                properties.getProperty("hub.email.template.directory"), properties.getProperty("logo.image"), "Test Subject Line");

        emailChannel.sendMessage(event, emailConfigEntity);
    }

}
