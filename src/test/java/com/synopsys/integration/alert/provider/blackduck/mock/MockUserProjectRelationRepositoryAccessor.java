package com.synopsys.integration.alert.provider.blackduck.mock;

import java.util.HashSet;
import java.util.Set;

import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelation;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserProjectRelationRepositoryAccessor;
import com.synopsys.integration.alert.database.relation.DatabaseRelation;

public class MockUserProjectRelationRepositoryAccessor extends UserProjectRelationRepositoryAccessor {
    private final Set<UserProjectRelation> userProjectRelations = new HashSet<>();

    public MockUserProjectRelationRepositoryAccessor() {
        super(null);
    }

    public DatabaseRelation addUserProjectRelation(final Long userId, final Long projectId) {
        final UserProjectRelation newRelation = new UserProjectRelation(userId, projectId);
        userProjectRelations.add(newRelation);
        return newRelation;
    }

    public void deleteAllRelations() {
        userProjectRelations.clear();
    }

    public Set<UserProjectRelation> getUserProjectRelations() {
        return userProjectRelations;
    }
}
