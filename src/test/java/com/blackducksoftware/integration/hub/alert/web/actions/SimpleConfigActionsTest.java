package com.blackducksoftware.integration.hub.alert.web.actions;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.blackducksoftware.integration.hub.alert.mock.model.MockRestModelUtil;
import com.blackducksoftware.integration.hub.alert.mock.model.global.MockGlobalRestModelUtil;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public abstract class SimpleConfigActionsTest<R extends ConfigRestModel> {

    protected SimpleConfigActions<R> configActions;

    @Before
    public void setup() {
        configActions = getConfigActions();
    }

    public abstract SimpleConfigActions<R> getConfigActions();

    @Test
    public void testFailedValidateConfig() {
        final MockGlobalRestModelUtil<R> mockRestModel = getMockRestModelUtil();
        final Map<String, String> fieldErrors = new HashMap<>();
        configActions.validateConfig(mockRestModel.createEmptyGlobalRestModel(), fieldErrors);

        assertEquals(getRequiredFieldErrorCount(), fieldErrors.keySet().size());
    }

    public abstract MockGlobalRestModelUtil<R> getMockRestModelUtil();

    public abstract int getRequiredFieldErrorCount();
}
