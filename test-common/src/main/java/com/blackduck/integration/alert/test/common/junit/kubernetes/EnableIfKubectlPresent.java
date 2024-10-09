package com.blackduck.integration.alert.test.common.junit.kubernetes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.jupiter.api.extension.ExtendWith;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(KubectlInstalledCondition.class)
public @interface EnableIfKubectlPresent {
}
