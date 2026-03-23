import React from 'react';
import { DISTRIBUTION_INFO } from 'page/distribution/DistributionModel';
import * as PropTypes from 'prop-types';
import DistributionTable from 'page/distribution/DistributionTable';
import PageLayout from 'common/component/PageLayout';

const DistributionConfiguration = ({ readonly }) => (
    <PageLayout
        title={DISTRIBUTION_INFO.label}
        description={DISTRIBUTION_INFO.description}
        headerIcon={['fas', 'tasks']}
    >
        <DistributionTable readonly={readonly}/>
    </PageLayout>
);

DistributionConfiguration.propTypes = {
    readonly: PropTypes.bool
};

export default DistributionConfiguration;