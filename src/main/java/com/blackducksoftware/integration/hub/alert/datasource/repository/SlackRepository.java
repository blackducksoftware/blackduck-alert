/*
 * Copyright (C) 2017 Black Duck Software Inc.
 * http://www.blackducksoftware.com/
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of
 * Black Duck Software ("Confidential Information"). You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Black Duck Software.
 */
package com.blackducksoftware.integration.hub.alert.datasource.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import com.blackducksoftware.integration.hub.alert.datasource.entity.SlackConfigEntity;

@Transactional
public interface SlackRepository extends JpaRepository<SlackConfigEntity, Long> {

}
