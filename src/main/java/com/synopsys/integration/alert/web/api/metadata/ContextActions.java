package com.synopsys.integration.alert.web.api.metadata;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.api.ReadAllAction;
import com.synopsys.integration.alert.web.api.metadata.model.ConfigContextsResponseModel;

@Component
public class ContextActions implements ReadAllAction<ConfigContextsResponseModel> {
    @Override
    public ActionResponse<ConfigContextsResponseModel> getAll() {
        return new ActionResponse<>(HttpStatus.OK, ConfigContextsResponseModel.DEFAULT);
    }
}
