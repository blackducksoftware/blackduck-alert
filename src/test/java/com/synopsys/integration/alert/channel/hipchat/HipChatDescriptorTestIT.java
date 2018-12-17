package com.synopsys.integration.alert.channel.hipchat;

public class HipChatDescriptorTestIT {//extends DescriptorTestConfigTest<HipChatDistributionConfig, HipChatDistributionConfigEntity, HipChatGlobalConfigEntity, HipChatEventProducer> {

    //FIXME fix the tests
    //    @Autowired
    //    private HipChatDistributionRepositoryAccessor hipChatDistributionRepositoryAccessor;
    //    @Autowired
    //    private HipChatGlobalRepository hipChatRepository;
    //    @Autowired
    //    private HipChatDescriptor hipChatDescriptor;
    //
    //    @Override
    //    @Test
    //    public void testCreateChannelEvent() throws Exception {
    //        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
    //        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());
    //        final DatabaseEntity distributionEntity = getDistributionEntity();
    //        final String roomId = "12345";
    //        final Boolean notify = false;
    //        final String color = "purple";
    //        final HipChatDistributionConfig jobConfig = new HipChatDistributionConfig("1", roomId, notify, color, String.valueOf(distributionEntity.getId()), getDescriptor().getDestinationName(), "Test Job", "provider",
    //            FrequencyType.DAILY.name(), "true", "", Collections.emptyList(), Collections.emptyList(), FormatType.DIGEST.name());
    //
    //        final HipChatChannelEvent channelEvent = this.channelEvent.createChannelEvent(jobConfig, content);
    //
    //        assertEquals(Long.valueOf(1L), channelEvent.getCommonDistributionConfigId());
    //        assertEquals(36, channelEvent.getEventId().length());
    //        assertEquals(getDescriptor().getDestinationName(), channelEvent.getDestination());
    //        assertTrue(NumberUtils.toInt(roomId) == channelEvent.getRoomId());
    //        assertEquals(notify, channelEvent.getNotify());
    //        assertEquals(color, channelEvent.getColor());
    //    }
    //
    //    @Override
    //    public DatabaseEntity getDistributionEntity() {
    //        final MockHipChatEntity mockHipChatEntity = new MockHipChatEntity();
    //        final HipChatDistributionConfigEntity hipChatDistributionConfigEntity = mockHipChatEntity.createEntity();
    //        return hipChatDistributionRepositoryAccessor.saveEntity(hipChatDistributionConfigEntity);
    //    }
    //
    //    @Override
    //    public HipChatEventProducer createChannelEvent() {
    //        return new HipChatEventProducer();
    //    }
    //
    //    @Override
    //    public void cleanRepository() {
    //        hipChatRepository.deleteAll();
    //    }
    //
    //    @Override
    //    public void cleanDistributionRepositories() {
    //        hipChatDistributionRepositoryAccessor.deleteAll();
    //    }
    //
    //    @Override
    //    public void saveGlobalConfiguration() {
    //        final HipChatGlobalConfigEntity globalEntity = new HipChatGlobalConfigEntity(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_API_KEY), "");
    //        hipChatRepository.save(globalEntity);
    //    }
    //
    //    @Override
    //    public ChannelDescriptor getDescriptor() {
    //        return hipChatDescriptor;
    //    }
    //
    //    @Override
    //    public MockRestModelUtil<HipChatDistributionConfig> getFieldModel() {
    //        final MockHipChatRestModel restModel = new MockHipChatRestModel();
    //        restModel.setRoomId(properties.getProperty(TestPropertyKey.TEST_HIPCHAT_ROOM_ID));
    //        restModel.setNotify(false);
    //        restModel.setColor("random");
    //        return restModel;
    //    }

}
