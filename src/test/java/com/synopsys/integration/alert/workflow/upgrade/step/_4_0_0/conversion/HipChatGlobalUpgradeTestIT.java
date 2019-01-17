package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.hipchat.descriptor.HipChatDescriptor;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.deprecated.channel.hipchat.HipChatGlobalConfigEntity;
import com.synopsys.integration.alert.database.deprecated.channel.hipchat.HipChatGlobalRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class HipChatGlobalUpgradeTestIT extends AlertIntegrationTest {

    @Autowired
    private HipChatGlobalRepository repository;

    @Test
    public void testDataTransfer() {
        final HipChatGlobalUpgrade hipChatGlobalUpgrade = new HipChatGlobalUpgrade(repository, null, new FieldCreatorUtil());

        final String apiKey = "apiKey";
        final String hostServer = "hostServer";

        final HipChatGlobalConfigEntity hipChatGlobalConfigEntity = new HipChatGlobalConfigEntity(apiKey, hostServer);
        final HipChatGlobalConfigEntity savedEntity = repository.save(hipChatGlobalConfigEntity);

        final Map<String, ConfigurationFieldModel> fieldModelMap = hipChatGlobalUpgrade.convertEntityToFieldList(savedEntity).stream().collect(Collectors.toMap(config -> config.getFieldKey(), Function.identity()));

        assertEquals(2, fieldModelMap.entrySet().size());
        assertEquals(apiKey, fieldModelMap.get(HipChatDescriptor.KEY_API_KEY).getFieldValue().orElse(""));
        assertEquals(hostServer, fieldModelMap.get(HipChatDescriptor.KEY_HOST_SERVER).getFieldValue().orElse(""));
    }
}
