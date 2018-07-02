package com.blackducksoftware.integration.hub.alert.web.actions;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.datasource.entity.CommonDistributionConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.exception.AlertException;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockCommonDistributionEntity;
import com.blackducksoftware.integration.hub.alert.mock.entity.MockEntityUtil;
import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.hub.alert.web.model.distribution.CommonDistributionConfigRestModel;

public abstract class SimpleDistributionConfigActionsTest<D extends DatabaseEntity, R extends CommonDistributionConfigRestModel> {

    private SimpleDistributionConfigActions<D, R> configActions;

    @Before
    public void setup() {
        configActions = getConfigActions();
    }

    public abstract SimpleDistributionConfigActions<D, R> getConfigActions();

    @Test
    public void testFailedValidateConfig() {
        final MockRestModelUtil<R> mockRestModel = getMockRestModelUtil();
        final Map<String, String> fieldErrors = new HashMap<>();
        configActions.validateConfig(mockRestModel.createEmptyRestModel(), fieldErrors);

        assertEquals(getRequiredFieldErrorCount(), fieldErrors.keySet().size());
    }

    public abstract MockRestModelUtil<R> getMockRestModelUtil();

    public abstract int getRequiredFieldErrorCount();

    @Test
    public void testConstructRestModel() throws AlertException {
        final MockCommonDistributionEntity mockCommonUtil = new MockCommonDistributionEntity();
        final CommonDistributionConfigEntity commonEntity = mockCommonUtil.createEntity();

        final long commonId = commonEntity.getId();

        final MockEntityUtil<D> mockEntityUtil = getMockEntityUtil();
        final R restModel = configActions.constructRestModel(commonEntity, mockEntityUtil.createEntity());

        assertEquals(commonId, Long.parseLong(restModel.getId()));
    }

    public abstract MockEntityUtil<D> getMockEntityUtil();
}
