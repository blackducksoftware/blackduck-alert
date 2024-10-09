package com.blackduck.integration.alert.channel.jira.server.database.enumeration;

import java.util.stream.Stream;

import com.blackduck.integration.alert.channel.jira.server.model.enumeration.JiraServerAuthorizationMethod;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class JiraServerAuthorizationMethodConverter implements AttributeConverter<JiraServerAuthorizationMethod, Integer> {
    @Override
    public Integer convertToDatabaseColumn(JiraServerAuthorizationMethod jiraServerAuthorizationMethod) {
        if (jiraServerAuthorizationMethod == null) {
            return 0;
        }
        return jiraServerAuthorizationMethod.getMode();
    }

    @Override
    public JiraServerAuthorizationMethod convertToEntityAttribute(Integer mode) {
        if (mode == null) {
            return JiraServerAuthorizationMethod.BASIC;
        }

        return Stream.of(JiraServerAuthorizationMethod.values())
            .filter(authorizationMethod -> authorizationMethod.getMode().equals(mode))
            .findFirst()
            .orElseThrow(IllegalArgumentException::new);
    }
}
