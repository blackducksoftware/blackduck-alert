package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.component.scheduling.SchedulingUIConfig;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.deprecated.scheduling.SchedulingConfigEntity;
import com.synopsys.integration.alert.database.deprecated.scheduling.SchedulingRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class SchedulingUpgradeTestIT extends AlertIntegrationTest {

    @Autowired
    private SchedulingRepository repository;

    @Test
    public void dataTransferTest() {
        final SchedulingUpgrade schedulingUpgrade = new SchedulingUpgrade(repository, null, new FieldCreatorUtil());

        final String dailyDigest = "dailydigest";
        final String purgeFrequency = "purgeFrequency";
        final SchedulingConfigEntity schedulingConfigEntity = new SchedulingConfigEntity(dailyDigest, purgeFrequency);
        final SchedulingConfigEntity savedEntity = repository.save(schedulingConfigEntity);

        final Map<String, ConfigurationFieldModel> fieldModelMap = schedulingUpgrade.convertEntityToFieldList(savedEntity).stream().collect(Collectors.toMap(ConfigurationFieldModel::getFieldKey, Function.identity()));

        assertEquals(2, fieldModelMap.entrySet().size());
        assertEquals(dailyDigest, fieldModelMap.get(SchedulingUIConfig.KEY_DAILY_DIGEST_HOUR_OF_DAY).getFieldValue().orElse(""));
        assertEquals(purgeFrequency, fieldModelMap.get(SchedulingUIConfig.KEY_PURGE_DATA_FREQUENCY_DAYS).getFieldValue().orElse(""));
    }
}
