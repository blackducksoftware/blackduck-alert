import React from 'react';
import PropTypes from 'prop-types';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';
import ProviderTable from 'page/provider/ProviderTable';
import PageLayout from 'common/component/PageLayout';

const ProviderPageLayout = ({ readonly }) => (
    <PageLayout
        title={BLACKDUCK_INFO.label}
        description={BLACKDUCK_INFO.description}
        headerIcon="shield"
    >
        <ProviderTable readonly={readonly} />
    </PageLayout>
);

ProviderPageLayout.propTypes = {
    readonly: PropTypes.bool
};

export default ProviderPageLayout;
