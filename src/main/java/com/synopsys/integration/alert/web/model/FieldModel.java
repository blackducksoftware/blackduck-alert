package com.synopsys.integration.alert.web.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public class FieldModel extends Config {
    private final Map<String, Collection<String>> keyToValues;

    public FieldModel(final Map<String, Collection<String>> keyToValues) {
        this.keyToValues = keyToValues;
    }

    public Map<String, Collection<String>> getKeyToValues() {
        return keyToValues;
    }

    public Optional<String> getValue(String key) {
        return keyToValues.get(key).stream().findFirst();
    }

    public Collection<String> getValues(String key) {
        return keyToValues.get(key);
    }

    public void putString(String key, String value) {
        putStrings(key, Arrays.asList(value));
    }

    public void putStrings(String key, Collection<String> values) {
        keyToValues.put(key, values);
    }

}
