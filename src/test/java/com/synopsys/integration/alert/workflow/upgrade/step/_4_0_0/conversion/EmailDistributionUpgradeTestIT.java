package com.synopsys.integration.alert.workflow.upgrade.step._4_0_0.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedList;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import com.synopsys.integration.alert.channel.email.descriptor.EmailDescriptor;
import com.synopsys.integration.alert.database.api.configuration.model.ConfigurationFieldModel;
import com.synopsys.integration.alert.database.deprecated.channel.email.EmailGroupDistributionConfigEntity;
import com.synopsys.integration.alert.database.deprecated.channel.email.EmailGroupDistributionRepository;
import com.synopsys.integration.alert.util.AlertIntegrationTest;

public class EmailDistributionUpgradeTestIT extends AlertIntegrationTest {

    @Autowired
    EmailGroupDistributionRepository repository;

    @Test
    public void dataTransferTest() {
        CommonDistributionFieldCreator commonDistributionFieldCreator = Mockito.mock(CommonDistributionFieldCreator.class);
        Mockito.when(commonDistributionFieldCreator.createCommonFields(Mockito.anyString(), Mockito.anyLong())).thenReturn(new LinkedList<>());
        EmailDistributionUpgrade emailDistributionUpgrade = new EmailDistributionUpgrade(repository, null, commonDistributionFieldCreator, new FieldCreatorUtil());

        String logo = "logo";
        String subject = "subject";
        boolean projectOwnerOnly = true;
        EmailGroupDistributionConfigEntity emailGroupDistributionConfigEntity = new EmailGroupDistributionConfigEntity(logo, subject, projectOwnerOnly);
        final EmailGroupDistributionConfigEntity savedEntity = repository.save(emailGroupDistributionConfigEntity);

        final Map<String, ConfigurationFieldModel> fieldModelMap = emailDistributionUpgrade.convertEntityToFieldList(savedEntity).stream().collect(Collectors.toMap(ConfigurationFieldModel::getFieldKey, Function.identity()));

        assertEquals(1, fieldModelMap.entrySet().size());
        assertEquals(subject, fieldModelMap.get(EmailDescriptor.KEY_SUBJECT_LINE).getFieldValue().orElse(""));
    }
}
