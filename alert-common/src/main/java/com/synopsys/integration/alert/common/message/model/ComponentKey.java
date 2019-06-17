package com.synopsys.integration.alert.common.message.model;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public class ComponentKey extends AlertSerializableModel {
    private static final char KEY_SEPARATOR = '_';

    private final String category;
    private final String componentName;
    private final String componentValue;
    private final String subComponentName;
    private final String subComponentValue;
    private final String additionalData;

    public static String generateAdditionalDataString(Collection<LinkableItem> componentAttributes) {
        StringBuilder additionalData = new StringBuilder();
        for (LinkableItem attribute : componentAttributes) {
            if (attribute.isPartOfKey()) {
                if (additionalData.length() > 0) {
                    additionalData.append(", ");
                }
                additionalData.append(attribute.getName());
                additionalData.append(": ");
                additionalData.append(attribute.getValue());
            }
        }
        return additionalData.toString();
    }

    public ComponentKey(final String category, final String componentName, final String componentValue, final String subComponentName, final String subComponentValue, final String additionalData) {
        this.category = category;
        this.componentName = componentName;
        this.componentValue = componentValue;
        this.subComponentName = subComponentName;
        this.subComponentValue = subComponentValue;
        this.additionalData = additionalData;
    }

    public String getKey() {
        final List<String> keyParts = List.of(this.category, componentName, componentValue, subComponentName, subComponentValue, additionalData);
        return StringUtils.join(keyParts, KEY_SEPARATOR);
    }

    public String prettyPrint() {
        StringBuilder prettyPrintBuilder = new StringBuilder();
        prettyPrintBuilder.append(category);
        prettyPrintBuilder.append(" - ");
        prettyPrintBuilder.append(componentName);
        prettyPrintBuilder.append(": ");
        prettyPrintBuilder.append(componentValue);
        if (StringUtils.isNotBlank(subComponentName) && StringUtils.isNotBlank(subComponentValue)) {
            prettyPrintBuilder.append(", ");
            prettyPrintBuilder.append(subComponentName);
            prettyPrintBuilder.append(": ");
            prettyPrintBuilder.append(subComponentValue);
        }
        if (StringUtils.isNotBlank(additionalData)) {
            prettyPrintBuilder.append(", ");
            prettyPrintBuilder.append(additionalData);
        }
        return prettyPrintBuilder.toString();
    }

    @Override
    public boolean equals(final Object otherObject) {
        if (otherObject instanceof ComponentKey) {
            ComponentKey otherKey = (ComponentKey) otherObject;
            return this.getKey().equals(otherKey.getKey());
        }
        return false;
    }

}
