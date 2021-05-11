import React from 'react';
import { DISTRIBUTION_INFO } from 'page/distribution/DistributionModel';
import DistributionConfigurationTable from 'page/distribution/DistributionConfigurationTable';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';
import * as PropTypes from 'prop-types';

const DistributionConfiguration = ({
    csrfToken, errorHandler, readonly, showRefreshButton, descriptors
}) => (
    <CommonGlobalConfiguration
        label={DISTRIBUTION_INFO.label}
        description={DISTRIBUTION_INFO.description}
    >
        <DistributionConfigurationTable csrfToken={csrfToken} errorHandler={errorHandler} readonly={readonly} showRefreshButton={showRefreshButton} descriptors={descriptors} />
    </CommonGlobalConfiguration>
);

DistributionConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool,
    showRefreshButton: PropTypes.bool,
    descriptors: PropTypes.arrayOf(PropTypes.object)
};

DistributionConfiguration.defaultProps = {
    readonly: false,
    showRefreshButton: true,
    descriptors: []
};

export default DistributionConfiguration;
