package com.blackduck.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;

import com.blackduck.integration.alert.common.persistence.model.SettingsKeyModel;

public interface SettingsKeyAccessor {

    List<SettingsKeyModel> getSettingsKeys();

    Optional<SettingsKeyModel> getSettingsKeyByKey(final String key);

    SettingsKeyModel saveSettingsKey(final String key, final String value);

    void deleteSettingsKeyByKey(String key);

    void deleteSettingsKeyById(Long id);

}
