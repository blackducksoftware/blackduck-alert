package com.blackduck.integration.alert.web.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.Operation;

class CustomOperationNameGeneratorTest {

    private final CustomOperationNameGenerator operationNameGenerator = new CustomOperationNameGenerator();

    private List<String> generateTagList(String... tagNamePrefixes) {
        List<String> tagNames = new ArrayList<>(tagNamePrefixes.length);
        for (String prefix : tagNamePrefixes) {
            tagNames.add(String.format("%s%s%s", prefix, CustomOperationNameGenerator.TAG_NAME_COMPONENT_DELIMETER, CustomOperationNameGenerator.TAG_KEYWORD_CONTROLLER));
        }
        return tagNames;
    }

    @Controller
    private static class TestController {
        @GetMapping("/test-controller")
        public Object getOne() {
            return new Object();
        }
    }

    @RestController
    private static class TestRestController {
        @GetMapping("/test-rest-controller")
        public Object getOne() {
            return new Object();
        }
    }

    @Test
    void validControllerClass() throws Exception {
        Operation operation = new Operation();
        String tagName = "test";
        String methodName = "getOne";
        operation.setTags(generateTagList(tagName));
        Method method = Mockito.mock(Method.class);
        Mockito.when(method.getName()).thenReturn(methodName);
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), methodName);

        operationNameGenerator.customize(operation, handlerMethod);
        String expectedOperationId = String.format("api%s%s", StringUtils.capitalize(tagName), StringUtils.capitalize(methodName));
        assertEquals(expectedOperationId, operation.getOperationId());
    }

    @Test
    void validRestControllerClass() throws Exception {
        Operation operation = new Operation();
        String tagName = "test";
        String methodName = "getOne";
        operation.setTags(generateTagList(tagName));
        Method method = Mockito.mock(Method.class);
        Mockito.when(method.getName()).thenReturn(methodName);
        HandlerMethod handlerMethod = new HandlerMethod(new TestRestController(), methodName);

        operationNameGenerator.customize(operation, handlerMethod);
        String expectedOperationId = String.format("api%s%s", StringUtils.capitalize(tagName), StringUtils.capitalize(methodName));
        assertEquals(expectedOperationId, operation.getOperationId());
    }

    @Test
    void validControllerMultipleTagsClass() throws Exception {
        Operation operation = new Operation();
        String tagName = "test";
        String methodName = "getOne";
        operation.setTags(generateTagList(tagName, "test2", "test3"));
        Method method = Mockito.mock(Method.class);
        Mockito.when(method.getName()).thenReturn(methodName);
        HandlerMethod handlerMethod = new HandlerMethod(new TestController(), methodName);

        operationNameGenerator.customize(operation, handlerMethod);
        String expectedOperationId = String.format("api%s%s", StringUtils.capitalize(tagName), StringUtils.capitalize(methodName));
        assertEquals(expectedOperationId, operation.getOperationId());
    }

    @Test
    void validRestControlleMultipleTagsClass() throws Exception {
        Operation operation = new Operation();
        String tagName = "test";
        String methodName = "getOne";
        operation.setTags(generateTagList(tagName, "test2", "test3"));
        Method method = Mockito.mock(Method.class);
        Mockito.when(method.getName()).thenReturn(methodName);
        HandlerMethod handlerMethod = new HandlerMethod(new TestRestController(), methodName);

        operationNameGenerator.customize(operation, handlerMethod);
        String expectedOperationId = String.format("api%s%s", StringUtils.capitalize(tagName), StringUtils.capitalize(methodName));
        assertEquals(expectedOperationId, operation.getOperationId());
    }

    @Test
    void nonControllerNoOperationChangeTest() throws Exception {
        Operation operation = new Operation();
        String originalOperationId = "originalOperationId";
        String tagName = "test";
        String methodName = "getOne";
        operation.setTags(generateTagList(tagName));
        operation.setOperationId(originalOperationId);
        Method method = Mockito.mock(Method.class);
        Mockito.when(method.getName()).thenReturn(methodName);
        HandlerMethod handlerMethod = new HandlerMethod(new Object() {
            public Object getOne() {
                return new Object();
            }
        }, methodName);

        operationNameGenerator.customize(operation, handlerMethod);
        assertEquals(originalOperationId, operation.getOperationId());
    }

}
