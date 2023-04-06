import React from 'react';
import PageHeader from 'common/component/navigation/PageHeader';
import { BLACKDUCK_INFO, BLACKDUCK_URLS } from 'page/provider/blackduck/BlackDuckModel';
import ProviderTable from 'page/provider/ProviderTable';

const ProviderPageLayout = ({ readonly }) => (
    <div>
        <PageHeader
            title={BLACKDUCK_INFO.label}
            description={BLACKDUCK_INFO.description}
            icon="handshake"
        />
        <ProviderTable readonly={readonly}/>
    </div>
);

export default ProviderPageLayout;
