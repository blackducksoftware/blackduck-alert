/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert.web.api.about;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.blackduck.integration.alert.common.enumeration.DescriptorType;
import com.blackduck.integration.alert.common.persistence.accessor.SystemStatusAccessor;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.web.api.metadata.DescriptorMetadataActions;
import com.blackduck.integration.alert.web.api.metadata.model.DescriptorsResponseModel;
import com.blackduck.integration.alert.web.documentation.SwaggerConfiguration;
import com.blackduck.integration.util.ResourceUtil;
import com.google.gson.Gson;

@Component
public class AboutReader {
    public static final String PRODUCT_VERSION_UNKNOWN = "unknown";
    private final Logger logger = LoggerFactory.getLogger(AboutReader.class);

    private final Gson gson;
    private final SystemStatusAccessor systemStatusAccessor;
    private final DescriptorMetadataActions descriptorActions;

    @Autowired
    public AboutReader(Gson gson, SystemStatusAccessor systemStatusAccessor, DescriptorMetadataActions descriptorActions) {
        this.gson = gson;
        this.systemStatusAccessor = systemStatusAccessor;
        this.descriptorActions = descriptorActions;
    }

    public Optional<AboutModel> getAboutModel() {
        try {
            String aboutJson = ResourceUtil.getResourceAsString(getClass(), "/about.txt", StandardCharsets.UTF_8.toString());
            AboutModel aboutModel = gson.fromJson(aboutJson, AboutModel.class);
            String startupDate = systemStatusAccessor.getStartupTime() != null ? DateUtils.formatDateAsJsonString(systemStatusAccessor.getStartupTime()) : "";
            Set<DescriptorMetadata> providers = getDescriptorData(DescriptorType.PROVIDER);
            Set<DescriptorMetadata> channels = getDescriptorData(DescriptorType.CHANNEL);
            AboutModel model = new AboutModel(
                aboutModel.getVersion(),
                aboutModel.getCreated(),
                aboutModel.getDescription(),
                aboutModel.getProjectUrl(),
                aboutModel.getCommitHash(),
                aboutModel.getCopyrightYear(),
                createSwaggerUrl(SwaggerConfiguration.SWAGGER_DEFAULT_PATH_SPEC),
                systemStatusAccessor.isSystemInitialized(),
                startupDate,
                providers,
                channels
            );
            return Optional.of(model);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return Optional.empty();
        }
    }

    private Set<DescriptorMetadata> getDescriptorData(DescriptorType descriptorType) {
        Set<DescriptorMetadata> descriptorData = Set.of();
        ActionResponse<DescriptorsResponseModel> response = descriptorActions.getDescriptorsByType(descriptorType.name());
        if (response.hasContent()) {
            DescriptorsResponseModel providersData = response.getContent().get();
            descriptorData = providersData.getDescriptors();
        }
        return descriptorData;
    }

    public String getProductVersion() {
        Optional<AboutModel> aboutModel = getAboutModel();
        return aboutModel.map(AboutModel::getVersion)
                   .orElse(PRODUCT_VERSION_UNKNOWN);
    }

    private String createSwaggerUrl(String swaggerPath) {
        UriComponentsBuilder serverUrlBuilder = ServletUriComponentsBuilder.fromCurrentContextPath();
        serverUrlBuilder.pathSegment(swaggerPath);
        return serverUrlBuilder.toUriString();
    }

}
