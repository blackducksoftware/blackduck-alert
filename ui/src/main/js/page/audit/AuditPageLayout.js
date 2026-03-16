import React from 'react';
import PageLayout from 'common/component/PageLayout';
import AuditFailureTable from 'page/audit/AuditFailureTable';
import { AUDIT_INFO } from 'page/audit/AuditModel';

const AuditPageLayout = () => (
    <PageLayout
        title={AUDIT_INFO.label}
        description="Audit tracks all failed distribution events that have been produced by Alert. This page offers the ability to see why the event failed.
            The Audit failure table represents a grouping of data by notification.  Click on the View icon in any given row to display jobs associated
            with that notification."
        headerIcon="check"
    >
        <AuditFailureTable />
    </PageLayout>
);

export default AuditPageLayout;
