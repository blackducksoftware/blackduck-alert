package com.synopsys.integration.alert.provider.blackduck.mock;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserGroupRelation;
import com.synopsys.integration.alert.database.provider.blackduck.data.relation.UserGroupRelationRepositoryAccessor;
import com.synopsys.integration.alert.database.relation.DatabaseRelation;

public class MockUserGroupRelationRepositoryAccessor extends UserGroupRelationRepositoryAccessor {
    private final Set<UserGroupRelation> userGroupRelationSet = new HashSet<>();

    public MockUserGroupRelationRepositoryAccessor() {
        super(null);
    }

    public DatabaseRelation addUserGroupRelation(final UserGroupRelation userGroupRelation) {
        userGroupRelationSet.add(userGroupRelation);
        return userGroupRelation;
    }

    public List<UserGroupRelation> findByBlackDuckGroupId(final Long groupId) {
        final List<UserGroupRelation> matchingRelations = userGroupRelationSet.stream().filter(userGroupRelation -> userGroupRelation.getBlackDuckGroupId().equals(groupId)).collect(Collectors.toList());
        return matchingRelations;
    }

    public void deleteAllRelations() {
        userGroupRelationSet.clear();
    }

    public Set<UserGroupRelation> getUserGroupRelationSet() {
        return userGroupRelationSet;
    }
}
