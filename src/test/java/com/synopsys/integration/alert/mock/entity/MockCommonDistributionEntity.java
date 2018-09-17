package com.synopsys.integration.alert.mock.entity;

import com.google.gson.JsonObject;
import com.synopsys.integration.alert.channel.hipchat.HipChatChannel;
import com.synopsys.integration.alert.common.enumeration.FrequencyType;
import com.synopsys.integration.alert.database.entity.CommonDistributionConfigEntity;
import com.synopsys.integration.alert.web.model.CommonDistributionConfig;
import com.synopsys.integration.alert.web.model.Config;

public class MockCommonDistributionEntity extends MockEntityUtil<CommonDistributionConfigEntity> {
    private Long distributionConfigId;
    private String distributionType;
    private String name;
    private FrequencyType frequency;
    private Boolean filterByProject;
    private Long id;
    private String providerName;

    public MockCommonDistributionEntity() {
        this(1L, HipChatChannel.COMPONENT_NAME.toString(), "Name", "provider_blackduck", FrequencyType.REAL_TIME, true, 2L);
    }

    private MockCommonDistributionEntity(final Long distributionConfigId, final String distributionType, final String name, final String providerName, final FrequencyType frequency, final Boolean filterByProject, final Long id) {
        super();
        this.distributionConfigId = distributionConfigId;
        this.distributionType = distributionType;
        this.name = name;
        this.frequency = frequency;
        this.filterByProject = filterByProject;
        this.id = id;
        this.providerName = providerName;
    }

    public Long getDistributionConfigId() {
        return distributionConfigId;
    }

    public void setDistributionConfigId(final Long distributionConfigId) {
        this.distributionConfigId = distributionConfigId;
    }

    public String getDistributionType() {
        return distributionType;
    }

    public void setDistributionType(final String distributionType) {
        this.distributionType = distributionType;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    public FrequencyType getFrequency() {
        return frequency;
    }

    public void setFrequency(final FrequencyType frequency) {
        this.frequency = frequency;
    }

    public Boolean getFilterByProject() {
        return filterByProject;
    }

    public void setFilterByProject(final Boolean filterByProject) {
        this.filterByProject = filterByProject;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    @Override
    public Config createConfig() {
        return new CommonDistributionConfig(id.toString(), distributionConfigId.toString(), distributionType, name, providerName, frequency.toString(), filterByProject.toString(),
            null, null);
    }

    @Override
    public Config createEmptyConfig() {
        return new CommonDistributionConfig();
    }

    @Override
    public CommonDistributionConfigEntity createEntity() {
        final CommonDistributionConfigEntity entity = new CommonDistributionConfigEntity(distributionConfigId, distributionType, name, providerName, frequency, filterByProject);
        entity.setId(id);
        return entity;
    }

    @Override
    public CommonDistributionConfigEntity createEmptyEntity() {
        return new CommonDistributionConfigEntity();
    }

    @Override
    public String getEntityJson() {
        final JsonObject json = new JsonObject();
        json.addProperty("id", id);
        json.addProperty("distributionConfigId", distributionConfigId);
        json.addProperty("distributionType", distributionType);
        json.addProperty("name", name);
        json.addProperty("providerName", providerName);
        json.addProperty("frequency", frequency.name());
        json.addProperty("filterByProject", filterByProject);
        return json.toString();
    }

}
