package com.synopsys.integration.alert.api.distribution.mock;

import java.util.function.Function;

import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelation;
import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelationPK;
import com.synopsys.integration.alert.database.distribution.workflow.NotificationCorrelationToNotificationRelationRepository;
import com.synopsys.integration.alert.test.common.database.MockRepositoryContainer;

public class MockNotificationCorrelationToNotificationRelationRepository
    extends MockRepositoryContainer<NotificationCorrelationToNotificationRelationPK, NotificationCorrelationToNotificationRelation>
    implements NotificationCorrelationToNotificationRelationRepository {

    public MockNotificationCorrelationToNotificationRelationRepository(Function<NotificationCorrelationToNotificationRelation, NotificationCorrelationToNotificationRelationPK> idGenerator) {
        super(idGenerator);
    }
}
