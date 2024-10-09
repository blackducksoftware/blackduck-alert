/*
 * alert-database
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.database.job.azure.boards;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AzureBoardsJobDetailsRepository extends JpaRepository<AzureBoardsJobDetailsEntity, UUID> {
}
