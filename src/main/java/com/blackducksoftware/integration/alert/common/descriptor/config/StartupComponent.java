package com.blackducksoftware.integration.alert.common.descriptor.config;

import java.util.Set;

import com.blackducksoftware.integration.alert.web.model.Config;
import com.blackducksoftware.integration.alert.workflow.startup.AlertStartupProperty;

public abstract class StartupComponent {
    private final Config emptyConfigObject;

    public StartupComponent(final Config emptyConfigObject) {
        this.emptyConfigObject = emptyConfigObject;
    }

    public Config getGlobalRestModelObject() {
        return emptyConfigObject;
    }

    public abstract Set<AlertStartupProperty> getGlobalEntityPropertyMapping();

}
