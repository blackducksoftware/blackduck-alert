package com.synopsys.integration.alert.database.settings.proxy;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NonProxyHostsConfigurationRepository extends JpaRepository<NonProxyHostConfigurationEntity, NonProxyHostConfigurationPK> {
}
