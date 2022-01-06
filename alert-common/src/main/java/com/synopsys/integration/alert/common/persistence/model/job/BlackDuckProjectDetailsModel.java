/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.model.job;

import com.synopsys.integration.alert.api.common.model.AlertSerializableModel;

public class BlackDuckProjectDetailsModel extends AlertSerializableModel {
    private final String name;
    private final String href;

    public BlackDuckProjectDetailsModel(String name, String href) {
        this.name = name;
        this.href = href;
    }

    public String getName() {
        return name;
    }

    public String getHref() {
        return href;
    }

}
