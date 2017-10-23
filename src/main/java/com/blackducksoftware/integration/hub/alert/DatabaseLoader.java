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
package com.blackducksoftware.integration.hub.alert;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.repository.SettingRepository;
import com.blackducksoftware.integration.hub.alert.ui.model.Setting;

@Component
public class DatabaseLoader implements CommandLineRunner {
    private final SettingRepository repository;

    @Autowired
    public DatabaseLoader(final SettingRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(final String... args) throws Exception {
        this.repository.save(new Setting("test", "true", "boolean"));
    }

}
