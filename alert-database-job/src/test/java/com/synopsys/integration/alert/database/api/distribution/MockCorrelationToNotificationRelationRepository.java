package com.synopsys.integration.alert.database.api.distribution;

import java.util.function.Function;

import com.synopsys.integration.alert.database.distribution.workflow.MockRepositoryContainer;
import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelation;
import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelationPK;
import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelationRepository;

public class MockCorrelationToNotificationRelationRepository
    extends MockRepositoryContainer<NotificationCorrelationToNotificationRelationPK, NotificationCorrelationToNotificationRelation>
    implements NotificationCorrelationToNotificationRelationRepository {
    private static Function<NotificationCorrelationToNotificationRelation, NotificationCorrelationToNotificationRelationPK> createIDGenerator() {
        return relation -> new NotificationCorrelationToNotificationRelationPK(
            relation.getNotificationCorrelationId(),
            relation.getNotificationId()
        );
    }

    public MockCorrelationToNotificationRelationRepository() {
        super(createIDGenerator());
    }
}
