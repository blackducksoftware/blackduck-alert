package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.common.enumeration.EmailPropertyKeys;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.deprecated.channel.email.EmailGlobalConfigEntity;
import com.synopsys.integration.alert.database.deprecated.channel.email.EmailGlobalRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class EmailGlobalUpgradeTestIT extends AlertIntegrationTest {

    @Autowired
    private EmailGlobalRepository repository;

    @Test
    public void dataTransferTest() {
        final EmailGlobalUpgrade emailGlobalUpgrade = new EmailGlobalUpgrade(repository, null, new FieldCreatorUtil());

        final String host = "host";
        final Integer port = 80;
        final String saslRealm = "saslRealm";
        final EmailGlobalConfigEntity emailGlobalConfigEntity = new EmailGlobalConfigEntity(host, null, null, port, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
            null, null, null, null, null, saslRealm, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        final EmailGlobalConfigEntity savedEntity = repository.save(emailGlobalConfigEntity);

        final Map<String, ConfigurationFieldModel> fieldModelMap = emailGlobalUpgrade.convertEntityToFieldList(savedEntity).stream().collect(Collectors.toMap(ConfigurationFieldModel::getFieldKey, Function.identity()));

        assertEquals(3, fieldModelMap.entrySet().size());
        assertEquals(host, fieldModelMap.get(EmailPropertyKeys.JAVAMAIL_HOST_KEY.getPropertyKey()).getFieldValue().orElse(""));
        assertEquals(saslRealm, fieldModelMap.get(EmailPropertyKeys.JAVAMAIL_SASL_REALM_KEY.getPropertyKey()).getFieldValue().orElse(""));
        assertEquals(String.valueOf(port), fieldModelMap.get(EmailPropertyKeys.JAVAMAIL_PORT_KEY.getPropertyKey()).getFieldValue().orElse("0"));
    }
}
