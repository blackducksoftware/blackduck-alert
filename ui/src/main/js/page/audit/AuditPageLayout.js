import React from 'react';
import PageHeader from 'common/component/navigation/PageHeader';
import AuditFailureTable from 'page/audit/AuditFailureTable';
import { AUDIT_INFO } from 'page/audit/AuditModel';

const AuditPageLayout = () => (
    <div>
        <PageHeader
            title={AUDIT_INFO.label}
            description="Audit tracks all failed distribution events that have been produced by Alert. This page offers the ability to see why the event failed.
                 The Audit failure table represents a grouping of data by notification.  Click on the View icon in any given row to display jobs associated
                with that notification."
            icon="check"
        />
        <AuditFailureTable />
    </div>
);

export default AuditPageLayout;
