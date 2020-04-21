package com.synopsys.integration.alert.database.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.synopsys.integration.alert.common.persistence.model.SettingsKeyModel;
import com.synopsys.integration.alert.database.api.mock.MockSettingsKeyRepository;
import com.synopsys.integration.alert.database.settings.SettingsKeyEntity;
import com.synopsys.integration.alert.database.settings.SettingsKeyRepository;

public class DefaultSettingsKeyAccessorTest {

    @Test
    public void getSettingsKeysTest() {
        final String key = "key1";
        final String key2 = "key2";
        final String value1 = "value1";
        final String value2 = "value2";

        SettingsKeyEntity settingsKeyEntity1 = new SettingsKeyEntity(key, value1);
        SettingsKeyEntity settingsKeyEntity2 = new SettingsKeyEntity(key2, value2);
        settingsKeyEntity1.setId(1L);
        settingsKeyEntity2.setId(2L);

        SettingsKeyRepository settingsKeyRepository = new MockSettingsKeyRepository();
        settingsKeyRepository.save(settingsKeyEntity1);
        settingsKeyRepository.save(settingsKeyEntity2);

        DefaultSettingsKeyAccessor settingsKeyAccessor = new DefaultSettingsKeyAccessor(settingsKeyRepository);

        List<SettingsKeyModel> settingsKeyModelList = settingsKeyAccessor.getSettingsKeys();

        assertEquals(2, settingsKeyModelList.size());
        SettingsKeyModel settingsKeyModel1 = settingsKeyModelList.get(0);
        SettingsKeyModel settingsKeyModel2 = settingsKeyModelList.get(1);
        assertEquals(key, settingsKeyModel1.getKey());
        assertEquals(value1, settingsKeyModel1.getValue());
        assertEquals(key2, settingsKeyModel2.getKey());
        assertEquals(value2, settingsKeyModel2.getValue());
    }

    @Test
    public void getSettingsByKeyTest() {
        final String key = "key1";
        final String value1 = "value1";

        SettingsKeyEntity settingsKeyEntity = new SettingsKeyEntity(key, value1);
        settingsKeyEntity.setId(1L);

        SettingsKeyRepository settingsKeyRepository = new MockSettingsKeyRepository();
        settingsKeyRepository.save(settingsKeyEntity);

        DefaultSettingsKeyAccessor settingsKeyAccessor = new DefaultSettingsKeyAccessor(settingsKeyRepository);
        Optional<SettingsKeyModel> settingsKeyModelOptional = settingsKeyAccessor.getSettingsKeyByKey("key1");
        Optional<SettingsKeyModel> settingsKeyModelOptionalNull = settingsKeyAccessor.getSettingsKeyByKey("-1");

        assertTrue(settingsKeyModelOptional.isPresent());
        assertFalse(settingsKeyModelOptionalNull.isPresent());
        SettingsKeyModel settingsKeyModel = settingsKeyModelOptional.get();
        assertEquals(key, settingsKeyModel.getKey());
        assertEquals(value1, settingsKeyModel.getValue());
    }

    @Test
    public void saveSettingsKeyTest() {
        final String key = "key1";
        final String key2 = "key2";
        final String originalValue = "originalValue";
        final String newValue = "newValue";
        final String newValue2 = "newValue-2";

        SettingsKeyEntity settingsKeyEntity = new SettingsKeyEntity(key, originalValue);
        settingsKeyEntity.setId(1L);

        SettingsKeyRepository settingsKeyRepository = new MockSettingsKeyRepository();
        settingsKeyRepository.save(settingsKeyEntity);

        DefaultSettingsKeyAccessor settingsKeyAccessor = new DefaultSettingsKeyAccessor(settingsKeyRepository);
        SettingsKeyModel settingsKeyModel = settingsKeyAccessor.saveSettingsKey(key, newValue);
        SettingsKeyModel settingsKeyModelKeyNotPresent = settingsKeyAccessor.saveSettingsKey(key2, newValue2);

        assertEquals(key, settingsKeyModel.getKey());
        assertEquals(newValue, settingsKeyModel.getValue());
        assertEquals(key2, settingsKeyModelKeyNotPresent.getKey());
        assertEquals(newValue2, settingsKeyModelKeyNotPresent.getValue());
    }

    @Test
    public void deleteSettingsKeyByKeyTest() {
        final String key = "key1";
        final String originalValue = "originalValue";

        SettingsKeyEntity settingsKeyEntity = new SettingsKeyEntity(key, originalValue);
        settingsKeyEntity.setId(1L);

        SettingsKeyRepository settingsKeyRepository = new MockSettingsKeyRepository();
        settingsKeyRepository.save(settingsKeyEntity);

        DefaultSettingsKeyAccessor settingsKeyAccessor = new DefaultSettingsKeyAccessor(settingsKeyRepository);

        assertTrue(settingsKeyRepository.findByKey(key).isPresent());
        settingsKeyAccessor.deleteSettingsKeyByKey(key);
        assertFalse(settingsKeyRepository.findByKey(key).isPresent());
    }
}
