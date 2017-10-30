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
package com.blackducksoftware.integration.hub.alert.web.controller;

import org.springframework.http.ResponseEntity;

import com.blackducksoftware.integration.exception.IntegrationException;
import com.blackducksoftware.integration.hub.alert.datasource.entity.DatabaseEntity;
import com.blackducksoftware.integration.hub.alert.web.model.ConfigRestModel;

public interface ConfigController<D extends DatabaseEntity, R extends ConfigRestModel> {
    public ResponseEntity<String> postConfig(final R restModel) throws IntegrationException;

    public ResponseEntity<String> putConfig(final R restModel) throws IntegrationException;

    public ResponseEntity<String> validateConfig(R restModel);

    public ResponseEntity<String> deleteConfig(final R restModel);

    public ResponseEntity<String> testConfig(final R restModel) throws IntegrationException;

}
