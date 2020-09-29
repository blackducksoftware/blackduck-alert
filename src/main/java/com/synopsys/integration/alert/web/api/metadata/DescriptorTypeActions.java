package com.synopsys.integration.alert.web.api.metadata;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.api.ReadAllAction;
import com.synopsys.integration.alert.web.api.metadata.model.DescriptorTypesResponseModel;

@Component
public class DescriptorTypeActions implements ReadAllAction<DescriptorTypesResponseModel> {

    @Override
    public ActionResponse<DescriptorTypesResponseModel> getAll() {
        return new ActionResponse<>(HttpStatus.OK, DescriptorTypesResponseModel.DEFAULT);
    }
}
