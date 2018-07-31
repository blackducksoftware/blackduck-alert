package com.blackducksoftware.integration.alert.common;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.blackducksoftware.integration.alert.web.model.AboutModel;
import com.blackducksoftware.integration.util.ResourceUtil;
import com.google.gson.Gson;

@Component
public class AboutReader {
    public final static String PRODUCT_VERSION_UNKNOWN = "unknown";
    private final static Logger logger = LoggerFactory.getLogger(AboutReader.class);
    private final Gson gson;

    @Autowired
    public AboutReader(final Gson gson) {
        this.gson = gson;
    }

    public AboutModel readAboutInformation() {
        AboutModel aboutModel;
        try {
            final String aboutJson = ResourceUtil.getResourceAsString(getClass(), "/about.txt", StandardCharsets.UTF_8.toString());
            aboutModel = gson.fromJson(aboutJson, AboutModel.class);
        } catch (final Exception e) {
            aboutModel = null;
            logger.error(e.getMessage(), e);
        }
        return aboutModel;
    }

    public String getProductVersion() {
        final AboutModel aboutModel = readAboutInformation();
        if (aboutModel != null) {
            return aboutModel.getVersion();
        } else {
            return PRODUCT_VERSION_UNKNOWN;
        }
    }
}
