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

import java.lang.reflect.Type;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.blackducksoftware.integration.hub.alert.channel.email.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@Controller
public class EmailConfigController {
    private final EmailRepository emailRepository;
    private final Gson gson;

    @Autowired
    EmailConfigController(final EmailRepository emailRepository, final Gson gson) {
        this.emailRepository = emailRepository;
        this.gson = gson;
    }

    @RequestMapping(value = "/config/email", method = RequestMethod.GET)
    public @ResponseBody String getConfig() {
        final Type listOfEmailConfigEntity = new TypeToken<List<EmailConfigEntity>>() {
        }.getType();
        final List<EmailConfigEntity> configurations = emailRepository.findAll();
        return gson.toJson(configurations, listOfEmailConfigEntity);
    }
}
