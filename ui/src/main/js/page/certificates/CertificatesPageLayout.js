import React from 'react';
import PropTypes from 'prop-types';

import { Tab } from 'react-bootstrap';
import PageLayout from 'common/component/PageLayout';
import ViewTabs from 'common/component/navigation/ViewTabs';
import CertificatesTable from 'page/certificates/CertificatesTable';
import MTLSCertificateLayout from 'page/certificates/MTLSCertificateLayout';
import { CERTIFICATE_INFO } from 'page/certificates/CertificateModel';

const CertificatesPageLayout = ({ csrfToken, errorHandler, readOnly }) => (
    <PageLayout
        title={CERTIFICATE_INFO.label}
        description="This page allows you to configure certificates for Alert to establish secure communication."
        headerIcon="award"
    >
        <ViewTabs defaultActiveKey={1} id="certificate-tabs">
            <Tab eventKey={1} title="Server">
                <CertificatesTable readOnly={readOnly} />
            </Tab>
            <Tab eventKey={2} title="Client">
                <MTLSCertificateLayout
                    csrfToken={csrfToken}
                    errorHandler={errorHandler}
                />
            </Tab>
        </ViewTabs>
    </PageLayout>
);

CertificatesPageLayout.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    readOnly: PropTypes.bool.isRequired
};

export default CertificatesPageLayout;
