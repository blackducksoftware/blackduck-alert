import React from 'react';
import { DISTRIBUTION_INFO } from 'distribution/DistributionModel';
import DistributionConfigurationTable from 'distribution/DistributionConfigurationTable';
import CommonGlobalConfiguration from 'global/CommonGlobalConfiguration';
import * as PropTypes from 'prop-types';

const DistributionConfigurationV2 = ({
    csrfToken, readonly, showRefreshButton, descriptors
}) => (
    <CommonGlobalConfiguration
        label={DISTRIBUTION_INFO.label}
        description={DISTRIBUTION_INFO.description}
    >
        <DistributionConfigurationTable csrfToken={csrfToken} readonly={readonly} showRefreshButton={showRefreshButton} descriptors={descriptors} />
    </CommonGlobalConfiguration>
);

DistributionConfigurationV2.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    showRefreshButton: PropTypes.bool,
    descriptors: PropTypes.arrayOf(PropTypes.object)
};

DistributionConfigurationV2.defaultProps = {
    readonly: false,
    showRefreshButton: true,
    descriptors: []
};

export default DistributionConfigurationV2;
