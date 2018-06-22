package com.blackducksoftware.integration.hub.alert.mock.entity;

import com.blackducksoftware.integration.hub.alert.channel.hipchat.HipChatChannel;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.google.gson.JsonObject;

public class MockCommonDistributionEntity extends MockEntityUtil<CommonDistributionConfigEntity> {
    private Long distributionConfigId;
    private String distributionType;
    private String name;
    private DigestTypeEnum frequency;
    private Boolean filterByProject;
    private Long id;

    public MockCommonDistributionEntity() {
        this(1L, HipChatChannel.COMPONENT_NAME.toString(), "Name", DigestTypeEnum.REAL_TIME, true, 2L);
    }

    private MockCommonDistributionEntity(final Long distributionConfigId, final String distributionType, final String name, final DigestTypeEnum frequency, final Boolean filterByProject, final Long id) {
        super();
        this.distributionConfigId = distributionConfigId;
        this.distributionType = distributionType;
        this.name = name;
        this.frequency = frequency;
        this.filterByProject = filterByProject;
        this.id = id;
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

    public DigestTypeEnum getFrequency() {
        return frequency;
    }

    public void setFrequency(final DigestTypeEnum frequency) {
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
    public CommonDistributionConfigEntity createEntity() {
        final CommonDistributionConfigEntity entity = new CommonDistributionConfigEntity(distributionConfigId, distributionType, name, frequency, filterByProject);
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
        json.addProperty("frequency", frequency.name());
        json.addProperty("filterByProject", filterByProject);
        return json.toString();
    }

}
