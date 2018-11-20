package com.synopsys.integration.alert.channel.email;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

import org.junit.Test;

import com.synopsys.integration.alert.common.model.AggregateMessageContent;
import com.synopsys.integration.alert.common.model.LinkableItem;
import com.synopsys.integration.alert.database.entity.DatabaseEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckProjectEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.BlackDuckUserEntity;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.provider.blackduck.BlackDuckProvider;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckProjectRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockBlackDuckUserRepositoryAccessor;
import com.synopsys.integration.alert.provider.blackduck.mock.MockUserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.web.channel.model.EmailDistributionConfig;
import com.synopsys.integration.rest.RestConstants;

public class EmailEventProducerTest {

    @Test
    public void createEmailEvent() throws Exception {
        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = EmailGroupChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;
        final String formatType = "FORMAT";
        final String subjectLine = "Alert unit test";

        final EmailDistributionConfig emailDistributionConfig = new EmailDistributionConfig(commonDistributionConfigId.toString(), distributionConfigId.toString(), distributionType, "Job Name",
            providerName, "REAL_TIME", "FALSE", null, subjectLine, "", false, Collections.emptyList(), Collections.emptyList(), formatType);

        final MockBlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();
        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final MockUserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor = new MockUserProjectRelationRepositoryAccessor();

        final EmailEventProducer emailEventProducer = new EmailEventProducer(blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);

        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "", null, subTopic, Collections.emptyList());

        final EmailChannelEvent expected = new EmailChannelEvent(RestConstants.formatDate(new Date()), providerName, formatType,
            content, commonDistributionConfigId, Collections.emptySet(), subjectLine);

        final EmailChannelEvent event = emailEventProducer.createChannelEvent(emailDistributionConfig, content);
        assertEquals(expected.getAuditEntryId(), event.getAuditEntryId());
        assertEquals(expected.getDestination(), event.getDestination());
        assertEquals(expected.getProvider(), event.getProvider());
        assertEquals(expected.getFormatType(), event.getFormatType());
        assertEquals(expected.getContent(), event.getContent());
        assertEquals(expected.getSubjectLine(), event.getSubjectLine());
        assertEquals(expected.getEmailAddresses(), event.getEmailAddresses());
    }

    @Test
    public void createEmailEventWithEmailAddresses() throws Exception {
        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = EmailGroupChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;
        final String formatType = "FORMAT";
        final String subjectLine = "Alert unit test";

        final EmailDistributionConfig emailDistributionConfig = new EmailDistributionConfig(commonDistributionConfigId.toString(), distributionConfigId.toString(), distributionType, "Job Name",
            providerName, "REAL_TIME", "FALSE", null, subjectLine, "", false, Collections.emptyList(), Collections.emptyList(), formatType);

        final MockBlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();
        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final MockUserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor = new MockUserProjectRelationRepositoryAccessor();

        final DatabaseEntity projectEntity1 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project one", "", "", ""));

        final String email1 = "Test Email 1";
        final String email2 = "Test Email 2";
        final DatabaseEntity userEntity1 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email1, false));
        final DatabaseEntity userEntity2 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email2, false));

        final UserProjectRelation userProjectRelation1 = new UserProjectRelation(userEntity1.getId(), projectEntity1.getId());
        final UserProjectRelation userProjectRelation2 = new UserProjectRelation(userEntity2.getId(), projectEntity1.getId());

        userProjectRelationRepositoryAccessor.deleteAndSaveAll(new HashSet<>(Arrays.asList(userProjectRelation1, userProjectRelation2)));

        final EmailEventProducer emailEventProducer = new EmailEventProducer(blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);

        final LinkableItem subTopic = new LinkableItem("subTopic", "Alert has sent this test message", null);
        final AggregateMessageContent content = new AggregateMessageContent("testTopic", "Project one", null, subTopic, Collections.emptyList());

        final EmailChannelEvent expected = new EmailChannelEvent(RestConstants.formatDate(new Date()), providerName, formatType,
            content, commonDistributionConfigId, new HashSet<>(Arrays.asList(email1, email2)), subjectLine);

        final EmailChannelEvent event = emailEventProducer.createChannelEvent(emailDistributionConfig, content);
        assertEquals(expected.getAuditEntryId(), event.getAuditEntryId());
        assertEquals(expected.getDestination(), event.getDestination());
        assertEquals(expected.getProvider(), event.getProvider());
        assertEquals(expected.getContent(), event.getContent());
        assertEquals(expected.getSubjectLine(), event.getSubjectLine());
        assertEquals(expected.getEmailAddresses(), event.getEmailAddresses());
    }

    @Test
    public void createTestEmailEvent() throws Exception {
        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = EmailGroupChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;
        final String formatType = "FORMAT";
        final String subjectLine = "Alert unit test";

        final MockBlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();
        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final MockUserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor = new MockUserProjectRelationRepositoryAccessor();

        final EmailEventProducer emailEventProducer = new EmailEventProducer(blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);

        final AggregateMessageContent testContent = emailEventProducer.createTestNotificationContent();

        final EmailChannelEvent expectedTest = new EmailChannelEvent(RestConstants.formatDate(new Date()), providerName, formatType,
            testContent, commonDistributionConfigId, Collections.emptySet(), subjectLine);

        final EmailDistributionConfig emailDistributionConfig = new EmailDistributionConfig(commonDistributionConfigId.toString(), distributionConfigId.toString(), distributionType, "Test Email Job", BlackDuckProvider.COMPONENT_NAME,
            "REAL_TIME", "false", "", subjectLine, "", false, Collections.emptyList(), Collections.emptyList(), "DEFAULT");

        final EmailChannelEvent testEvent = emailEventProducer.createChannelTestEvent(emailDistributionConfig);
        assertEquals(expectedTest.getAuditEntryId(), testEvent.getAuditEntryId());
        assertEquals(expectedTest.getDestination(), testEvent.getDestination());
        assertEquals(expectedTest.getProvider(), testEvent.getProvider());
        assertEquals(expectedTest.getContent(), testEvent.getContent());
        assertEquals(expectedTest.getSubjectLine(), testEvent.getSubjectLine());
        assertEquals(expectedTest.getEmailAddresses(), testEvent.getEmailAddresses());

    }

    @Test
    public void createTestEmailEventWithEmailAddressesIncludeAllProjects() throws Exception {
        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = EmailGroupChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;
        final String formatType = "FORMAT";
        final String subjectLine = "Alert unit test";

        final MockBlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();
        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final MockUserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor = new MockUserProjectRelationRepositoryAccessor();

        final DatabaseEntity projectEntity1 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project one", "", "", ""));
        final DatabaseEntity projectEntity2 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project two", "", "", ""));
        final DatabaseEntity projectEntity3 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project three", "", "", ""));
        final DatabaseEntity projectEntity4 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity("Project four", "", "", ""));

        final String email1 = "Test Email 1";
        final String email2 = "Test Email 2";
        final DatabaseEntity userEntity1 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email1, false));
        final DatabaseEntity userEntity2 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email2, false));

        final UserProjectRelation userProjectRelation1 = new UserProjectRelation(userEntity1.getId(), projectEntity1.getId());
        final UserProjectRelation userProjectRelation2 = new UserProjectRelation(userEntity1.getId(), projectEntity2.getId());
        final UserProjectRelation userProjectRelation3 = new UserProjectRelation(userEntity2.getId(), projectEntity3.getId());
        final UserProjectRelation userProjectRelation4 = new UserProjectRelation(userEntity2.getId(), projectEntity4.getId());

        userProjectRelationRepositoryAccessor.deleteAndSaveAll(new HashSet<>(Arrays.asList(userProjectRelation1, userProjectRelation2, userProjectRelation3, userProjectRelation4)));

        final EmailEventProducer emailEventProducer = new EmailEventProducer(blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);

        final AggregateMessageContent testContent = emailEventProducer.createTestNotificationContent();

        final EmailChannelEvent expectedTest = new EmailChannelEvent(RestConstants.formatDate(new Date()), providerName, formatType,
            testContent, commonDistributionConfigId, new HashSet<>(Arrays.asList(email1, email2)), subjectLine);

        final EmailDistributionConfig emailDistributionConfig = new EmailDistributionConfig(commonDistributionConfigId.toString(), distributionConfigId.toString(), distributionType, "Test Email Job", BlackDuckProvider.COMPONENT_NAME,
            "REAL_TIME", "false", "", subjectLine, "", false, Collections.emptyList(), Collections.emptyList(), "DEFAULT");

        final EmailChannelEvent testEvent = emailEventProducer.createChannelTestEvent(emailDistributionConfig);
        assertEquals(expectedTest.getAuditEntryId(), testEvent.getAuditEntryId());
        assertEquals(expectedTest.getDestination(), testEvent.getDestination());
        assertEquals(expectedTest.getProvider(), testEvent.getProvider());
        assertEquals(expectedTest.getContent(), testEvent.getContent());
        assertEquals(expectedTest.getSubjectLine(), testEvent.getSubjectLine());
        assertEquals(expectedTest.getEmailAddresses(), testEvent.getEmailAddresses());

    }

    @Test
    public void createTestEmailEventWithEmailAddresses() throws Exception {
        final Long commonDistributionConfigId = 25L;
        final Long distributionConfigId = 33L;
        final String distributionType = EmailGroupChannel.COMPONENT_NAME;
        final String providerName = BlackDuckProvider.COMPONENT_NAME;
        final String formatType = "FORMAT";
        final String subjectLine = "Alert unit test";

        final MockBlackDuckProjectRepositoryAccessor blackDuckProjectRepositoryAccessor = new MockBlackDuckProjectRepositoryAccessor();
        final MockBlackDuckUserRepositoryAccessor blackDuckUserRepositoryAccessor = new MockBlackDuckUserRepositoryAccessor();
        final MockUserProjectRelationRepositoryAccessor userProjectRelationRepositoryAccessor = new MockUserProjectRelationRepositoryAccessor();

        final String project1 = "Project one";
        final String project2 = "Project two";
        final DatabaseEntity projectEntity1 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity(project1, "", "", ""));
        final DatabaseEntity projectEntity2 = blackDuckProjectRepositoryAccessor.saveEntity(new BlackDuckProjectEntity(project2, "", "", ""));

        final String email1 = "Test Email 1";
        final String email2 = "Test Email 2";
        final String email3 = "Test Email 3";
        final String email4 = "Test Email 4";
        final DatabaseEntity userEntity1 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email1, false));
        final DatabaseEntity userEntity2 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email2, false));
        final DatabaseEntity userEntity3 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email3, false));
        final DatabaseEntity userEntity4 = blackDuckUserRepositoryAccessor.saveEntity(new BlackDuckUserEntity(email4, false));

        final UserProjectRelation userProjectRelation1 = new UserProjectRelation(userEntity1.getId(), projectEntity1.getId());
        final UserProjectRelation userProjectRelation2 = new UserProjectRelation(userEntity2.getId(), projectEntity2.getId());
        final UserProjectRelation userProjectRelation3 = new UserProjectRelation(userEntity3.getId(), projectEntity2.getId());
        final UserProjectRelation userProjectRelation4 = new UserProjectRelation(userEntity4.getId(), projectEntity2.getId());

        userProjectRelationRepositoryAccessor.deleteAndSaveAll(new HashSet<>(Arrays.asList(userProjectRelation1, userProjectRelation2, userProjectRelation3, userProjectRelation4)));

        final EmailEventProducer emailEventProducer = new EmailEventProducer(blackDuckProjectRepositoryAccessor, blackDuckUserRepositoryAccessor, userProjectRelationRepositoryAccessor);

        final AggregateMessageContent testContent = emailEventProducer.createTestNotificationContent();

        final EmailChannelEvent expectedTest = new EmailChannelEvent(RestConstants.formatDate(new Date()), providerName, formatType,
            testContent, commonDistributionConfigId, new HashSet<>(Arrays.asList(email1, email2, email3, email4)), subjectLine);

        final EmailDistributionConfig emailDistributionConfig = new EmailDistributionConfig(commonDistributionConfigId.toString(), distributionConfigId.toString(), distributionType, "Test Email Job", BlackDuckProvider.COMPONENT_NAME,
            "REAL_TIME", "true", "", subjectLine, "", false, Arrays.asList(project1, project2), Collections.emptyList(), "DEFAULT");

        final EmailChannelEvent testEvent = emailEventProducer.createChannelTestEvent(emailDistributionConfig);
        assertEquals(expectedTest.getAuditEntryId(), testEvent.getAuditEntryId());
        assertEquals(expectedTest.getDestination(), testEvent.getDestination());
        assertEquals(expectedTest.getProvider(), testEvent.getProvider());
        assertEquals(expectedTest.getContent(), testEvent.getContent());
        assertEquals(expectedTest.getSubjectLine(), testEvent.getSubjectLine());
        assertEquals(expectedTest.getEmailAddresses(), testEvent.getEmailAddresses());

    }
}
