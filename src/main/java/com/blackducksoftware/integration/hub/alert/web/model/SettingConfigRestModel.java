/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.web.model;

public class SettingConfigRestModel extends ConfigRestModel {
    private static final long serialVersionUID = -2951359362486811013L;

    private String settingName;
    private String settingValue;
    private String settingType;

    protected SettingConfigRestModel() {
    }

    public SettingConfigRestModel(final String settingName, final String settingValue, final String settingType) {
        this.settingName = settingName;
        this.settingValue = settingValue;
        this.settingType = settingType;
    }

    public String getSettingName() {
        return settingName;
    }

    public void setSettingName(final String settingName) {
        this.settingName = settingName;
    }

    public String getSettingValue() {
        return settingValue;
    }

    public void setSettingValue(final String settingValue) {
        this.settingValue = settingValue;
    }

    public String getSettingType() {
        return settingType;
    }

    public void setSettingType(final String settingType) {
        this.settingType = settingType;
    }
}
