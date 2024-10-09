/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.documentation;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.customizers.GlobalOperationCustomizer;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.Operation;

/* SpringDocs will generate an operation ID, which is an optional field for OpenAPI compliant REST APIs,
  when the UI for the REST API documentation is accessed.
  The operation ID that is generated is unique, because according to the OpenAPI Spec it must be,
  however, it is the method name plus a numeric suffix i.e. getOne, getOne1, ..., getOne15.
  Alert uses an interface to ensure a consistent REST API for users of Alert and developers.
  The method names are the same across multiple controllers due to using the interfaces to ensure a consistent contract first REST API design.
  The names that are generated provide little context to the method when the numeric suffixes are added, because the method names across
  controllers are not unique.
  If a new controller is added and is processed before another controller that previously existed,
  then the numeric suffix used on the operation ID may be different for a previously existing controller.

  Use this class to take the class name and the method name to create an operation ID that has a name that has more contextual meaning.
  Generate the operation ID using 3 components to the operation ID in the form: <prefix><context><suffix> where:
  1. prefix = api
  2. context = first operation tag name
  3. suffix = method name

  Examples:
    - apiAboutGetAbout
    - apiAuditEntryGetOne
    - apiAuditEntryLegacyGet
    - apiCertificatesGetOne
 */
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
