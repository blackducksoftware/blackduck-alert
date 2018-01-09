package com.blackducksoftware.integration.hub.alert.mock.entity;

import com.blackducksoftware.integration.hub.alert.channel.SupportedChannels;
import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.enumeration.DigestTypeEnum;
import com.google.gson.JsonObject;

public class MockCommonDistributionEntity extends MockEntityUtil<CommonDistributionConfigEntity> {
    private final Long distributionConfigId;
    private final String distributionType;
    private final String name;
    private final DigestTypeEnum frequency;
    private final Boolean filterByProject;
    private final Long id;

    public MockCommonDistributionEntity() {
        this(1L, SupportedChannels.HIPCHAT.toString(), "Name", DigestTypeEnum.REAL_TIME, true, 2L);
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

    public String getDistributionType() {
        return distributionType;
    }

    public String getName() {
        return name;
    }

    public DigestTypeEnum getFrequency() {
        return frequency;
    }

    public Boolean getFilterByProject() {
        return filterByProject;
    }

    @Override
    public Long getId() {
        return id;
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
