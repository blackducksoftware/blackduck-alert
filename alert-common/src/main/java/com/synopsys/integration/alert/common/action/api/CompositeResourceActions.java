package com.synopsys.integration.alert.common.action.api;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.action.ValidationActionResponse;
import com.synopsys.integration.alert.common.rest.model.AlertSerializableModel;

public interface CompositeResourceActions<T extends AlertSerializableModel, I> {
    ActionResponse<T> create(T resource);

    ActionResponse<T> getOne(I id);

    ActionResponse<T> update(I id, T resource);

    ActionResponse<T> delete(I id);

    ActionResponse<? extends MultiResponseModel<T>> getAll();

    ValidationActionResponse test(T resource);

    ValidationActionResponse validate(T resource);

}
