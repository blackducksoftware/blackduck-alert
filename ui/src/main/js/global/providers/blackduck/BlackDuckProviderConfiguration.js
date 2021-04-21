import React from 'react';
import * as PropTypes from 'prop-types';
import { BLACKDUCK_INFO } from 'global/providers/blackduck/BlackDuckModel';
import CommonGlobalConfiguration from 'global/CommonGlobalConfiguration';
import BlackDuckProviderTable from 'global/providers/blackduck/BlackDuckProviderTable';

const BlackDuckProviderConfiguration = ({ csrfToken, readonly }) => (
    <CommonGlobalConfiguration
        label={BLACKDUCK_INFO.label}
        description={BLACKDUCK_INFO.description}
    >
        <BlackDuckProviderTable csrfToken={csrfToken} readonly={readonly} />
    </CommonGlobalConfiguration>
);

BlackDuckProviderConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool
};

BlackDuckProviderConfiguration.defaultProps = {
    readonly: false
};

export default BlackDuckProviderConfiguration;
