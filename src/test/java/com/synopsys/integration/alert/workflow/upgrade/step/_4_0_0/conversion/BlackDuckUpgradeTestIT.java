package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.deprecated.blackduck.GlobalBlackDuckConfigEntity;
import com.synopsys.integration.alert.database.deprecated.blackduck.GlobalBlackDuckRepository;
import com.synopsys.integration.alert.provider.blackduck.descriptor.BlackDuckDescriptor;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class BlackDuckUpgradeTestIT extends AlertIntegrationTest {

    @Autowired
    private GlobalBlackDuckRepository repository;

    @Test
    public void dataTransferTest() {
        final BlackDuckUpgrade blackDuckUpgrade = new BlackDuckUpgrade(repository, null, new FieldCreatorUtil());

        final Integer timeout = 300;
        final String apiKey = "apiKey";
        final String url = "url";
        final GlobalBlackDuckConfigEntity globalBlackDuckConfigEntity = new GlobalBlackDuckConfigEntity(timeout, apiKey, url);
        final GlobalBlackDuckConfigEntity savedEntity = repository.save(globalBlackDuckConfigEntity);

        final Map<String, ConfigurationFieldModel> fieldModelMap = blackDuckUpgrade.convertEntityToFieldList(savedEntity).stream().collect(Collectors.toMap(ConfigurationFieldModel::getFieldKey, Function.identity()));

        assertEquals(3, fieldModelMap.entrySet().size());
        assertEquals(String.valueOf(timeout), fieldModelMap.get(BlackDuckDescriptor.KEY_BLACKDUCK_TIMEOUT).getFieldValue().orElse("0"));
        assertEquals(apiKey, fieldModelMap.get(BlackDuckDescriptor.KEY_BLACKDUCK_API_KEY).getFieldValue().orElse(""));
        assertEquals(url, fieldModelMap.get(BlackDuckDescriptor.KEY_BLACKDUCK_URL).getFieldValue().orElse(""));
    }
}
