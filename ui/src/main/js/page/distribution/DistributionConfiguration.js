import React from 'react';
import { DISTRIBUTION_INFO } from 'page/distribution/DistributionModel';
import * as PropTypes from 'prop-types';
import DistributionTable from 'page/distribution/DistributionTable';
import PageLayout from 'common/component/PageLayout';

const DistributionConfiguration = ({ descriptors }) => (
    <PageLayout
        title={DISTRIBUTION_INFO.label}
        description={DISTRIBUTION_INFO.description}
        headerIcon={['fas', 'tasks']}
    >
        <DistributionTable descriptors={descriptors} />
    </PageLayout>
);

DistributionConfiguration.propTypes = {
    descriptors: PropTypes.object.isRequired
};

export default DistributionConfiguration;
