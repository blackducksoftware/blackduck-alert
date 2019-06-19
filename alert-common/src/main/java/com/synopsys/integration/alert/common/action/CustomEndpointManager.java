package com.synopsys.integration.alert.common.action;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.exception.AlertException;
import com.synopsys.integration.alert.common.rest.model.FieldValueModel;

@Component
public class CustomEndpointManager {
    public static final String CUSTOM_ENDPOINT_URL = "/api/function";
    private final Map<String, Function<Map<String, FieldValueModel>, ResponseEntity<String>>> endpointFunctions = new HashMap<>();

    public boolean containsFunction(final String functionKey) {
        return endpointFunctions.containsKey(functionKey);
    }

    public void registerFunction(final String functionKey, final Function<Map<String, FieldValueModel>, ResponseEntity<String>> endpointFunction) throws AlertException {
        if (containsFunction(functionKey)) {
            throw new AlertException("A custom endpoint is already registered for " + functionKey);
        }
        endpointFunctions.put(functionKey, endpointFunction);
    }

    public ResponseEntity<String> performFunction(final String endpointKey, final Map<String, FieldValueModel> fieldModelValues) {
        if (!containsFunction(endpointKey)) {
            return new ResponseEntity("Not functionality has been created for this endpoint.", HttpStatus.NOT_IMPLEMENTED);
        }

        return endpointFunctions.get(endpointKey).apply(fieldModelValues);
    }
}
