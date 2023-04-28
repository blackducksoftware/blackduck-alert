import React from 'react';
import { DISTRIBUTION_INFO } from 'page/distribution/DistributionModel';
import * as PropTypes from 'prop-types';
import DistributionTable from 'page/distribution/DistributionTable';
import PageHeader from 'common/component/navigation/PageHeader';

const DistributionConfiguration = ({
    readonly
}) => (
    <>
        <PageHeader
            title={DISTRIBUTION_INFO.label}
            description={DISTRIBUTION_INFO.description}
            icon={['fas', 'tasks']}
        />
        <DistributionTable readonly={readonly}/>
    </>
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
