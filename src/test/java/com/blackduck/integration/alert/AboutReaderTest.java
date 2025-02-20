/*
 * blackduck-alert
 *
 * Copyright (c) 2024 Black Duck Software, Inc.
 *
 * Use subject to the terms and conditions of the Black Duck Software End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.blackduck.integration.alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.blackduck.integration.alert.common.action.ActionResponse;
import com.blackduck.integration.alert.common.descriptor.config.ui.DescriptorMetadata;
import com.blackduck.integration.alert.common.persistence.model.SystemMessageModel;
import com.blackduck.integration.alert.common.util.DateUtils;
import com.blackduck.integration.alert.database.job.api.DefaultSystemStatusAccessor;
import com.blackduck.integration.alert.database.system.DefaultSystemMessageAccessor;
import com.blackduck.integration.alert.web.api.about.AboutModel;
import com.blackduck.integration.alert.web.api.about.AboutReader;
import com.blackduck.integration.alert.web.api.metadata.DescriptorMetadataActions;
import com.blackduck.integration.alert.web.api.metadata.model.DescriptorsResponseModel;
import com.blackduck.integration.blackduck.service.BlackDuckServicesFactory;
import com.blackduck.integration.rest.RestConstants;

public class AboutReaderTest {
    private DefaultSystemStatusAccessor defaultSystemStatusUtility;
    private DefaultSystemMessageAccessor defaultSystemMessageUtility;
    private DescriptorMetadataActions descriptorMetadataActions;

    @BeforeEach
    public void initialize() {
        defaultSystemStatusUtility = Mockito.mock(DefaultSystemStatusAccessor.class);
        Mockito.when(defaultSystemStatusUtility.isSystemInitialized()).thenReturn(Boolean.TRUE);
        Mockito.when(defaultSystemStatusUtility.getStartupTime()).thenReturn(DateUtils.createCurrentDateTimestamp());

        defaultSystemMessageUtility = Mockito.mock(DefaultSystemMessageAccessor.class);
        Mockito.when(defaultSystemMessageUtility.getSystemMessages()).thenReturn(Collections.singletonList(new SystemMessageModel("1", RestConstants.formatDate(new Date()), "ERROR", "startup errors", "type")));

        descriptorMetadataActions = Mockito.mock(DescriptorMetadataActions.class);
        DescriptorsResponseModel descriptorsResponseModel = new DescriptorsResponseModel(Set.of(Mockito.mock(DescriptorMetadata.class), Mockito.mock(DescriptorMetadata.class)));
        Mockito.when(descriptorMetadataActions.getDescriptorsByType(Mockito.anyString()))
            .thenReturn(new ActionResponse<>(HttpStatus.OK, descriptorsResponseModel));
    }

    @Test
    public void testAboutReadNull() {
        AboutReader reader = new AboutReader(null, defaultSystemStatusUtility, descriptorMetadataActions);
        Optional<AboutModel> aboutModel = reader.getAboutModel();
        assertTrue(aboutModel.isEmpty());
    }

    @Test
    public void testAboutRead() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        AboutReader reader = new AboutReader(BlackDuckServicesFactory.createDefaultGson(), defaultSystemStatusUtility, descriptorMetadataActions);
        Optional<AboutModel> aboutModel = reader.getAboutModel();
        assertTrue(aboutModel.isPresent());
    }

    @Test
    public void testAboutReadVersionUnknown() {
        AboutReader reader = new AboutReader(null, defaultSystemStatusUtility, descriptorMetadataActions);
        String version = reader.getProductVersion();
        assertEquals(AboutReader.PRODUCT_VERSION_UNKNOWN, version);
    }

    @Test
    public void testAboutReadVersion() {
        AboutReader reader = new AboutReader(BlackDuckServicesFactory.createDefaultGson(), defaultSystemStatusUtility, descriptorMetadataActions);
        String version = reader.getProductVersion();
        assertTrue(StringUtils.isNotBlank(version));
    }
}
