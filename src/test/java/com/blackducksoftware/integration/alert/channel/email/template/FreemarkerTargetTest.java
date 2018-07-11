package com.blackducksoftware.integration.alert.channel.email.template;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.blackducksoftware.integration.alert.channel.email.template.FreemarkerTarget;

public class FreemarkerTargetTest {

    @Test
    public void testFreemarker() {
        final FreemarkerTarget freemarkerTarget = new FreemarkerTarget();

        final Map<String, String> target1 = new HashMap<>();
        target1.put("key1", "value1");

        final Map<String, String> target2 = new HashMap<>();
        target2.put("key2", "value2");

        freemarkerTarget.add(target1);
        freemarkerTarget.add(target2);

        final List<Map<String, String>> expected = Arrays.asList(target1, target2);

        assertEquals(expected, freemarkerTarget);
    }
}
