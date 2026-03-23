import React from 'react';
import PageLayout from 'common/component/PageLayout';
import { MSTEAMS_INFO } from 'page/channel/msteams/MSTeamsModel';

const MSTeamsGlobalConfiguration = () => (
    <PageLayout
        title={MSTEAMS_INFO.label}
        description="Configure MS Teams for Alert."
        headerIcon={['fab', 'windows']}
    />
);

export default MSTeamsGlobalConfiguration;
