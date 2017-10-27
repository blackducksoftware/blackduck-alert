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

import java.util.Collections;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.hub.alert.datasource.entity.HipChatConfigEntity;
import com.blackducksoftware.integration.hub.alert.datasource.repository.HipChatRepository;
import com.blackducksoftware.integration.hub.alert.web.ObjectTransformer;
import com.blackducksoftware.integration.hub.alert.web.model.HipChatConfigRestModel;

@Component
public class HipChatConfigActions extends ConfigActions<HipChatConfigEntity, HipChatConfigRestModel> {

    @Autowired
    public HipChatConfigActions(final HipChatRepository hipChatRepository, final ObjectTransformer objectTransformer) {
        super(HipChatConfigEntity.class, HipChatConfigRestModel.class, hipChatRepository, objectTransformer);
    }

    @Override
    public Map<String, String> validateConfig(final HipChatConfigRestModel restModel) {
        // TODO Auto-generated method stub
        return Collections.emptyMap();
    }

}
