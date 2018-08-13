package com.synopsys.integration.alert;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.springframework.core.convert.support.DefaultConversionService;

import com.google.gson.Gson;
import com.synopsys.integration.alert.channel.slack.mock.MockSlackRestModel;
import com.synopsys.integration.alert.common.ContentConverter;
import com.synopsys.integration.alert.web.channel.model.SlackDistributionConfig;

public class ContentConverterTest {

    @Test
    public void testLong() {
        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
        final long longActual = contentConverter.getLongValue("1");

        assertEquals(1, longActual);
    }

    @Test
    public void testGetContent() {
        final ContentConverter contentConverter = new ContentConverter(new Gson(), new DefaultConversionService());
        final MockSlackRestModel mockSlackRestModel = new MockSlackRestModel();

        final SlackDistributionConfig restModel = mockSlackRestModel.createRestModel();

        final String restModelJson = restModel.toString();
        final SlackDistributionConfig restModelActual = contentConverter.getJsonContent(restModelJson, SlackDistributionConfig.class);

        assertEquals(restModel, restModelActual);
    }
}
