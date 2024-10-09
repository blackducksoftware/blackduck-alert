package com.blackduck.integration.alert.database.authorization;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PermissionMatrixRepository extends JpaRepository<PermissionMatrixRelation, PermissionMatrixPK> {

    List<PermissionMatrixRelation> findAllByRoleId(Long roleId);

    List<PermissionMatrixRelation> findAllByRoleIdIn(Collection<Long> roleIds);

}
