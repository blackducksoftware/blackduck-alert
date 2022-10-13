package com.synopsys.integration.alert.test.common.junit.kubernetes;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@EnableIfKubectlPresent
@EnableIfHelmPresent
public @interface EnableIfKubeAndHelmPresent {
}
