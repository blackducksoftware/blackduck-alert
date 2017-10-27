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
package com.blackducksoftware.integration.hub.alert.web.actions;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.EmailConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.EmailRepository;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.EmailConfigRestModel;

@Component
public class EmailConfigActions extends ConfigActions<EmailConfigEntity, EmailConfigRestModel> {

    @Autowired
    public EmailConfigActions(final EmailRepository emailRepository, final ObjectTransformer objectTransformer) {
        super(EmailConfigEntity.class, EmailConfigRestModel.class, emailRepository, objectTransformer);
    }

    @Override
    public Map<String, String> validateConfig(final EmailConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return null;
    }

}
