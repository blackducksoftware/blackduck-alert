import React from 'react';
import * as PropTypes from 'prop-types';
import { BLACKDUCK_INFO } from 'provider/blackduck/BlackDuckModel';
import CommonGlobalConfiguration from 'global/CommonGlobalConfiguration';
import BlackDuckProviderTable from 'provider/blackduck/BlackDuckProviderTable';

const BlackDuckProviderConfiguration = ({ csrfToken, showRefreshButton, readonly }) => (
    <CommonGlobalConfiguration
        label={BLACKDUCK_INFO.label}
        description={BLACKDUCK_INFO.description}
    >
        <BlackDuckProviderTable csrfToken={csrfToken} readonly={readonly} showRefreshButton={showRefreshButton} />
    </CommonGlobalConfiguration>
);

BlackDuckProviderConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    showRefreshButton: PropTypes.bool.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool
};

BlackDuckProviderConfiguration.defaultProps = {
    readonly: false
};

export default BlackDuckProviderConfiguration;
