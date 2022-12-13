import React from 'react';
import PageHeader from 'common/component/navigation/PageHeader';
import CertificatesTable from 'page/certificates/CertificatesTable';
import { CERTIFICATE_INFO } from 'page/certificates/CertificateModel';

const CertificatesPageLayout = () => (
    <div>
        <PageHeader
            title={CERTIFICATE_INFO.label}
            description="This page allows you to configure certificates for Alert to establish secure communication."
            icon="award"
        />
        <CertificatesTable />
    </div>
);

export default CertificatesPageLayout;
