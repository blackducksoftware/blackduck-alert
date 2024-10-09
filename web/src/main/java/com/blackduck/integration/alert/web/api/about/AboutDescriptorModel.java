/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.about;

import com.blackduck.integration.alert.api.common.model.AlertSerializableModel;

public class AboutDescriptorModel extends AlertSerializableModel {
    private final String iconKey;
    private final String name;

    public AboutDescriptorModel(String iconKey, String name) {
        this.iconKey = iconKey;
        this.name = name;
    }

    public String getIconKey() {
        return iconKey;
    }

    public String getName() {
        return name;
    }

}
