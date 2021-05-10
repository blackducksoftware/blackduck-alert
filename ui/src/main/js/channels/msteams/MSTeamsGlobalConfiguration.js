import React from 'react';
import EmptyGlobalConfiguration from 'global/EmptyGlobalConfiguration';
import { MSTEAMS_INFO } from 'channels/msteams/MSTeamsModel';

const MSTeamsGlobalConfiguration = () => (
    <EmptyGlobalConfiguration label={MSTEAMS_INFO.label} description="Configure MS Teams for Alert." />
);

export default MSTeamsGlobalConfiguration;
