package com.blackducksoftware.integration.hub.alert.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import javax.persistence.Convert;

import com.blackducksoftware.integration.hub.alert.web.security.StringEncryptionConverter;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@SensitiveField
@Convert(converter = StringEncryptionConverter.class)
public @interface EncryptedStringField {

}
