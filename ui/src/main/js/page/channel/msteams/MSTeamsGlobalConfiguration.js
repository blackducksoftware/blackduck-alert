import React from 'react';
import PageHeader from 'common/component/navigation/PageHeader';
import { MSTEAMS_INFO } from 'page/channel/msteams/MSTeamsModel';

const MSTeamsGlobalConfiguration = () => (
    <PageHeader
        title={MSTEAMS_INFO.label}
        description="Configure MS Teams for Alert."
    />
);

export default MSTeamsGlobalConfiguration;
