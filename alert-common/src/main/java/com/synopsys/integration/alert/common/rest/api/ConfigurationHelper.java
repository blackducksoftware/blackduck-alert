package com.synopsys.integration.alert.common;

import java.util.Optional;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.action.ActionResponse;
import com.synopsys.integration.alert.common.enumeration.ConfigContextEnum;
import com.synopsys.integration.alert.common.security.authorization.AuthorizationManager;
import com.synopsys.integration.alert.descriptor.api.model.ChannelKey;

@Component
public class ConfigurationHelper {
    private final Logger logger = LoggerFactory.getLogger(ConfigurationHelper.class);
    private final AuthorizationManager authorizationManager;

    @Autowired
    public ConfigurationHelper(AuthorizationManager authorizationManager) {
        this.authorizationManager = authorizationManager;
    }

    public <T> ActionResponse<T> getOne(Supplier<Optional<T>> modelSupplier, ConfigContextEnum context, ChannelKey channel) {
        if (!authorizationManager.hasReadPermission(context, channel)) {
            return ActionResponse.createForbiddenResponse();
        }

        Optional<T> optionalResponse = modelSupplier.get();

        if (optionalResponse.isEmpty()) {
            return new ActionResponse<>(HttpStatus.NOT_FOUND);
        }

        return new ActionResponse<>(HttpStatus.OK, optionalResponse.get());
    }

}
