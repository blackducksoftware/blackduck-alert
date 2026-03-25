import React from 'react';
import PropTypes from 'prop-types';
import { Tab } from 'react-bootstrap';
import PageHeader from 'common/component/navigation/PageHeader';
import ViewTabs from 'common/component/navigation/ViewTabs';
import CertificatesTable from 'page/certificates/CertificatesTable';
import MTLSCertificateLayout from 'page/certificates/MTLSCertificateLayout';
import { CERTIFICATE_INFO } from 'page/certificates/CertificateModel';

const CertificatesPageLayout = ({ csrfToken, errorHandler }) => (
    <div>
        <PageHeader
            title={CERTIFICATE_INFO.label}
            description="This page allows you to configure certificates for Alert to establish secure communication."
            icon="award"
        />
        <ViewTabs defaultActiveKey={1} id="certificate-tabs">
            <Tab eventKey={1} title="Server">
                <CertificatesTable />
            </Tab>
            <Tab eventKey={2} title="Client">
                <MTLSCertificateLayout
                    csrfToken={csrfToken}
                    errorHandler={errorHandler}
                />
            </Tab>
        </ViewTabs>
    </div>
);

CertificatesPageLayout.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired
};

export default CertificatesPageLayout;
