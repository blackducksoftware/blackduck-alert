package com.blackducksoftware.integration.hub.alert.channel.email;

import com.blackducksoftware.integration.hub.alert.MockUtils;
import com.blackducksoftware.integration.hub.alert.channel.RestChannelTest;

// FIXME
public class EmailChannelTestIT extends RestChannelTest {
    private final MockUtils mockUtils = new MockUtils();

    // @Test
    // public void sendEmailTest() throws Exception {
    // Assume.assumeTrue(properties.containsKey("mail.smtp.host"));
    // Assume.assumeTrue(properties.containsKey("mail.smtp.from"));
    // Assume.assumeTrue(properties.containsKey("hub.email.template.directory"));
    // Assume.assumeTrue(properties.containsKey("logo.image"));
    //
    // Assume.assumeTrue(properties.containsKey("blackduck.hub.url"));
    // Assume.assumeTrue(properties.containsKey("blackduck.hub.username"));
    // Assume.assumeTrue(properties.containsKey("blackduck.hub.password"));
    //
    // final HubUsersRepository hubUsersRepository = Mockito.mock(HubUsersRepository.class);
    // final HubUsersEntity userEntity = mockUtils.createHubUsersEntity(properties.getProperty("blackduck.hub.username"));
    // Mockito.when(hubUsersRepository.findOne(Mockito.anyLong())).thenReturn(userEntity);
    //
    // final GlobalRepository globalRepository = Mockito.mock(GlobalRepository.class);
    // final GlobalHubConfigEntity globalConfig = new GlobalHubConfigEntity(300, properties.getProperty("blackduck.hub.username"), properties.getProperty("blackduck.hub.password"), "", "", "");
    // Mockito.when(globalRepository.findAll()).thenReturn(Arrays.asList(globalConfig));
    //
    // final List<VulnerabilityEntity> vulns = new ArrayList<>();
    // final VulnerabilityEntity vulnerability = new VulnerabilityEntity("Vuln ID", "Vuln Operation");
    // vulns.add(vulnerability);
    //
    // final HashMap<String, Object> itemModel = new HashMap<>();
    // itemModel.put(ItemTypeEnum.COMPONENT.toString(), "Manual Test Component");
    // itemModel.put(ItemTypeEnum.VERSION.toString(), "1.0.3");
    // itemModel.put(ItemTypeEnum.RULE.toString(), "Manual Policy Rule");
    // final ItemData data = new ItemData(itemModel);
    // final CategoryDataBuilder categoryBuilder = new CategoryDataBuilder();
    // categoryBuilder.addItem(data);
    // categoryBuilder.setCategoryKey(NotificationCategoryEnum.POLICY_VIOLATION.toString());
    //
    // final ProjectDataBuilder projectDataBuilder = new ProjectDataBuilder();
    // projectDataBuilder.setProjectName("Manual Test Project");
    // projectDataBuilder.setProjectVersion("Manual Test Project Version");
    // projectDataBuilder.setDigestType(DigestTypeEnum.REAL_TIME);
    // projectDataBuilder.addCategoryBuilder(NotificationCategoryEnum.POLICY_VIOLATION, categoryBuilder);
    // final ProjectData projectData = projectDataBuilder.build();
    //
    // final TestGlobalProperties globalProperties = new TestGlobalProperties(globalRepository);
    // globalProperties.hubUrl = properties.getProperty("blackduck.hub.url");
    //
    // final String trustCert = properties.getProperty("blackduck.hub.trust.cert");
    // if (trustCert != null) {
    // globalProperties.hubTrustCertificate = Boolean.valueOf(trustCert);
    // }
    //
    // final Gson gson = new Gson();
    // final EmailChannel emailChannel = new EmailChannel(globalProperties, gson, hubUsersRepository, null, null);
    // final EmailEvent event = new EmailEvent(projectData, userEntity.getId());
    //
    // final GlobalEmailConfigEntity emailConfigEntity = new GlobalEmailConfigEntity(properties.getProperty("mail.smtp.host"), null, null, null, null, null, properties.getProperty("mail.smtp.from"), null, null, null, null, null, null,
    // null, properties.getProperty("hub.email.template.directory"), properties.getProperty("logo.image"), "Test Subject Line");
    //
    // emailChannel.sendMessage(event, emailConfigEntity);
    // }

}
