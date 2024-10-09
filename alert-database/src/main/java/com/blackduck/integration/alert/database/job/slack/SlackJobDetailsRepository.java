/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.slack;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SlackJobDetailsRepository extends JpaRepository<SlackJobDetailsEntity, UUID> {
}
