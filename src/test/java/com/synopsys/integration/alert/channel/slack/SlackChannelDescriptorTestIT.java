package com.synopsys.integration.alert.channel.slack;

public class SlackChannelDescriptorTestIT { //} extends DescriptorTestConfigTest<SlackDistributionConfig, SlackDistributionConfigEntity, GlobalChannelConfigEntity, SlackEventProducer> {
    //FIXME fix tests
    //    @Autowired
    //    private SlackDistributionRepositoryAccessor slackDistributionRepositoryAccessor;
    //    @Autowired
    //    private SlackDescriptor slackDescriptor;
    //
    //    @Override
    //    @Test
    //    public void testCreateChannelEvent() throws Exception {
    //        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
    //        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());
    //        final DatabaseEntity distributionEntity = getDistributionEntity();
    //        final String webhook = "Webhook";
    //        final String channelUsername = "Username";
    //        final String channelName = "channel";
    //        final SlackDistributionConfig slackDistributionConfig = new SlackDistributionConfig("1", webhook, channelUsername, channelName,
    //            String.valueOf(distributionEntity.getId()), getDescriptor().getDestinationName(), "Test Job", "provider", FrequencyType.DAILY.name(), "true", "",
    //            Collections.emptyList(), Collections.emptyList(), FormatType.DIGEST.name());
    //
    //        final SlackChannelEvent channelEvent = this.channelEvent.createChannelEvent(slackDistributionConfig, content);
    //
    //        assertEquals(Long.valueOf(1L), channelEvent.getCommonDistributionConfigId());
    //        assertEquals(36, channelEvent.getEventId().length());
    //        assertEquals(getDescriptor().getDestinationName(), channelEvent.getDestination());
    //        assertEquals(webhook, channelEvent.getWebHook());
    //        assertEquals(channelUsername, channelEvent.getChannelUsername());
    //        assertEquals(channelName, channelEvent.getChannelName());
    //    }
    //
    //    @Override
    //    public DatabaseEntity getDistributionEntity() {
    //        final MockSlackEntity mockSlackEntity = new MockSlackEntity();
    //        final SlackDistributionConfigEntity slackDistributionConfigEntity = mockSlackEntity.createEntity();
    //        return slackDistributionRepositoryAccessor.saveEntity(slackDistributionConfigEntity);
    //    }
    //
    //    @Override
    //    public SlackEventProducer createChannelEvent() {
    //        return new SlackEventProducer();
    //    }
    //
    //    @Override
    //    public void cleanRepository() {
    //        // do nothing no global configuration
    //    }
    //
    //    @Override
    //    public void cleanDistributionRepositories() {
    //        slackDistributionRepositoryAccessor.deleteAll();
    //    }
    //
    //    @Override
    //    public void saveGlobalConfiguration() {
    //        // do nothing no global configuration
    //    }
    //
    //    @Override
    //    public ChannelDescriptor getDescriptor() {
    //        return slackDescriptor;
    //    }
    //
    //    @Override
    //    public MockRestModelUtil<SlackDistributionConfig> getFieldModel() {
    //        final MockSlackRestModel restModel = new MockSlackRestModel();
    //        restModel.setChannelName(this.properties.getProperty(TestPropertyKey.TEST_SLACK_CHANNEL_NAME));
    //        restModel.setChannelUsername(this.properties.getProperty(TestPropertyKey.TEST_SLACK_USERNAME));
    //        restModel.setWebhook(this.properties.getProperty(TestPropertyKey.TEST_SLACK_WEBHOOK));
    //        return restModel;
    //    }

}
