package com.blackducksoftware.integration.hub.alert.web.controller.distribution;

import org.springframework.beans.factory.annotation.Autowired;

import com.blackducksoftware.integration.hub.alert.datasource.entity.distribution.SlackDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.repository.SlackDistributionRepository;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockSlackEntity;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.hub.alert.mock.model.MockSlackRestModel;
import com.blackducksoftware.integration.hub.alert.web.controller.ControllerTest;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.SlackDistributionRestModel;

public class SlackDistributionConfigControllerTestIT extends ControllerTest<SlackDistributionConfigEntity, SlackDistributionRestModel, SlackDistributionRepository> {

    @Autowired
    SlackDistributionRepository slackDistributionRepository;

    @Override
    public SlackDistributionRepository getEntityRepository() {
        return slackDistributionRepository;
    }

    @Override
    public MockEntityUtil<SlackDistributionConfigEntity> getEntityMockUtil() {
        return new MockSlackEntity();
    }

    @Override
    public MockRestModelUtil<SlackDistributionRestModel> getRestModelMockUtil() {
        return new MockSlackRestModel();
    }

    @Override
    public String getRestControllerUrl() {
        return "/configuration/distribution/slack";
    }

}
