/*
 * alert-common
 *
 * Copyright (c) 2022 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.alert.common.persistence.accessor;

import java.util.List;
import java.util.Optional;

import com.synopsys.integration.alert.api.common.model.exception.AlertConfigurationException;
import com.synopsys.integration.alert.common.persistence.model.CustomCertificateModel;

public interface CustomCertificateAccessor {
    List<CustomCertificateModel> getCertificates();

    Optional<CustomCertificateModel> getCertificate(Long id);

    CustomCertificateModel storeCertificate(CustomCertificateModel certificateModel) throws AlertConfigurationException;

    void deleteCertificate(String certificateAlias);

    void deleteCertificate(Long certificateId);

}
