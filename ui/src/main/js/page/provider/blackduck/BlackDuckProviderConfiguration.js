import React from 'react';
import * as PropTypes from 'prop-types';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import BlackDuckProviderTable from 'page/provider/blackduck/BlackDuckProviderTable';

const BlackDuckProviderConfiguration = ({
    csrfToken, showRefreshButton, readonly, displayDelete
}) => (
    <CommonGlobalConfiguration
        label={BLACKDUCK_INFO.label}
        description={BLACKDUCK_INFO.description}
    >
        <BlackDuckProviderTable csrfToken={csrfToken} readonly={readonly} showRefreshButton={showRefreshButton} displayDelete={displayDelete} />
    </CommonGlobalConfiguration>
);

BlackDuckProviderConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    showRefreshButton: PropTypes.bool.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    displayDelete: PropTypes.bool
};

BlackDuckProviderConfiguration.defaultProps = {
    readonly: false,
    displayDelete: true
};

export default BlackDuckProviderConfiguration;
