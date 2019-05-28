package com.synopsys.integration.alert.workflow.startup.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class StartupComponent {
    private final Logger logger = LoggerFactory.getLogger(StartupComponent.class);

    private final Integer priorityWeight;
    private final String ComponentName;

    public StartupComponent(final Integer priorityWeight, final String componentName) {
        this.priorityWeight = priorityWeight;
        ComponentName = componentName;
    }

    public void initializeComponent() {
        final String runningStartupComponentLog = String.format("Running startup component %s with weight %s", getComponentName(), getPriorityWeight());
        logger.info(runningStartupComponentLog);
        runComponent();
    }

    public abstract void runComponent();

    public Integer getPriorityWeight() {
        return priorityWeight;
    }

    public String getComponentName() {
        return ComponentName;
    }
}
