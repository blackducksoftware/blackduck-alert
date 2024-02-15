package com.synopsys.integration.alert.web.documentation;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.Operation;

public class CustomOperationNameGenerator implements GlobalOperationCustomizer {
    public static final String TAG_KEYWORD_CONTROLLER = "controller";
    public static final String TAG_NAME_COMPONENT_DELIMETER = "-";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Operation customize(Operation operation, HandlerMethod handlerMethod) {
        Class<?> beanType = handlerMethod.getBeanType();
        boolean isControllerClass = beanType.isAnnotationPresent(Controller.class) || beanType.isAnnotationPresent(RestController.class);
        logger.debug("Processing Open API doc for type: {} controller:{}", beanType, isControllerClass);
        if (isControllerClass && !operation.getTags().isEmpty()) {
            logger.debug("Tags for operation: {}", operation.getTags());
            String[] tagNameSplit = StringUtils.split(operation.getTags().get(0), TAG_NAME_COMPONENT_DELIMETER);
            //remove the controller keyword from the tag name
            String tagNameAbbreviated = Arrays.stream(tagNameSplit)
                .filter(subString -> !subString.equalsIgnoreCase(TAG_KEYWORD_CONTROLLER))
                .map(StringUtils::capitalize)
                .reduce("", String::concat);
            String methodName = handlerMethod.getMethod().getName();
            String operationId = String.format("api%s%s", tagNameAbbreviated, StringUtils.capitalize(methodName));
            logger.debug("Operation Id old: {} new: {}", operation.getOperationId(), operationId);
            operation.setOperationId(operationId);
        }
        return operation;
    }
}
