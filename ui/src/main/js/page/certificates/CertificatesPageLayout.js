import React from 'react';
import PageHeader from 'common/component/navigation/PageHeader';
import CertificatesTable from 'page/certificates/CertificatesTable';
import MTLSCertificateLayout from 'page/certificates/MTLSCertificateLayout';
import { CERTIFICATE_INFO } from 'page/certificates/CertificateModel';
import { Tab, Tabs } from 'react-bootstrap';

const CertificatesPageLayout = ({ csrfToken, errorHandler }) => {
    return (
        <div>
            <PageHeader
                title={CERTIFICATE_INFO.label}
                description="This page allows you to configure certificates for Alert to establish secure communication."
                icon="award"
            />
            <Tabs defaultActiveKey={1} id="certificate-tabs">
                <Tab eventKey={1} title="Server">
                    <CertificatesTable />
                </Tab>
                <Tab eventKey={2} title="Client">
                    <MTLSCertificateLayout
                        csrfToken={csrfToken}
                        errorHandler={errorHandler}
                    />
                </Tab>
            </Tabs>
        </div>
    );
};

CertificatesPageLayout.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired
}

export default CertificatesPageLayout;
