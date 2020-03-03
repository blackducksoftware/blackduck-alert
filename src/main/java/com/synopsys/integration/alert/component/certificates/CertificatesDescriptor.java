/**
 * blackduck-alert
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.alert.component.certificates;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.synopsys.integration.alert.common.descriptor.ComponentDescriptor;

@Component
public class CertificatesDescriptor extends ComponentDescriptor {
    public static final String CERTIFICATES_LABEL = "Certificates";
    public static final String CERTIFICATES_URL = "certificates";
    public static final String CERTIFICATES_DESCRIPTION = "This page allows you to configure user certificates for Alert.";

    @Autowired
    public CertificatesDescriptor(CertificatesDescriptorKey descriptorKey, CertificatesUIConfig componentUIConfig) {
        super(descriptorKey, componentUIConfig);
    }

}
