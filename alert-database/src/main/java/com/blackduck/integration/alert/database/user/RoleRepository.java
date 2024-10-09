package com.blackduck.integration.alert.database.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByRoleName(String roleName);

    boolean existsRoleEntityByRoleName(String roleName);

    @Query("SELECT entity FROM RoleEntity entity WHERE entity.roleName IN (?1)")
    List<RoleEntity> findRoleEntitiesByRoleNames(Collection<String> roleIds);
}
