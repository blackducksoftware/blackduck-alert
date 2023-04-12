import React from 'react';
import PropTypes from 'prop-types';
import PageHeader from 'common/component/navigation/PageHeader';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';
import ProviderTable from 'page/provider/ProviderTable';

const ProviderPageLayout = ({ readonly }) => (
    <div>
        <PageHeader
            title={BLACKDUCK_INFO.label}
            description={BLACKDUCK_INFO.description}
            icon="handshake"
        />
        <ProviderTable readonly={readonly} />
    </div>
);

ProviderPageLayout.propTypes = {
    readonly: PropTypes.bool
};

export default ProviderPageLayout;
