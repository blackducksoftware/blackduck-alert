package com.synopsys.integration.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.gson.Gson;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.util.Stringable;

public class ContentConverterTest {
    private final Gson gson = new Application().gson();

    @Test
    public void testLong() {
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());
        final long longActual = contentConverter.getLongValue("1");

        assertEquals(1, longActual);
    }

    @Test
    public void testGetContent() {
        final ContentConverter contentConverter = new ContentConverter(gson, new DefaultConversionService());

        final InnerContent innerContent = new InnerContent();
        innerContent.field1 = "field 1";
        innerContent.field2 = "field 2";

        final ExampleContent exampleContent = new ExampleContent();
        exampleContent.innerObject = innerContent;
        exampleContent.exampleString = "example";
        exampleContent.exampleLong = 5L;
        exampleContent.exampleBoolean = Boolean.TRUE;
        exampleContent.exampleNullString = null;

        final String jsonContent = gson.toJson(exampleContent);
        final ExampleContent convertedContent = contentConverter.getJsonContent(jsonContent, ExampleContent.class);

        assertEquals(convertedContent, exampleContent);
    }

    private class ExampleContent extends Stringable {
        public InnerContent innerObject;
        public String exampleString;
        public Long exampleLong;
        public Boolean exampleBoolean;
        public String exampleNullString;
    }

    private class InnerContent extends Stringable {
        public String field1;
        public String field2;
    }
}
