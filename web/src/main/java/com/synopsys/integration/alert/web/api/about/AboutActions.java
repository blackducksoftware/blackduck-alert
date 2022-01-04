/*
 * web
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.web.api.about;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;

@Component
public class AboutActions {
    private final AboutReader aboutReader;

    @Autowired
    public AboutActions(AboutReader aboutReader) {
        this.aboutReader = aboutReader;
    }

    public ActionResponse<AboutModel> getAboutModel() {
        return aboutReader.getAboutModel()
                   .map(content -> new ActionResponse<>(HttpStatus.OK, content))
                   .orElse(new ActionResponse<>(HttpStatus.NOT_FOUND));
    }

}
