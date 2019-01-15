package com.synopsys.integration.alert.database.deprecated.blackduck;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GlobalBlackDuckRepository extends JpaRepository<GlobalBlackDuckConfigEntity, Long> {
}
