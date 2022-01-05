/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.common.persistence.model.SettingsKeyModel;

public interface SettingsKeyAccessor {

    List<SettingsKeyModel> getSettingsKeys();

    Optional<SettingsKeyModel> getSettingsKeyByKey(final String key);

    SettingsKeyModel saveSettingsKey(final String key, final String value);

    void deleteSettingsKeyByKey(String key);

    void deleteSettingsKeyById(Long id);

}
