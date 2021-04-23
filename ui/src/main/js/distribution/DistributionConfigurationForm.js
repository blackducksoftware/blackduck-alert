import React from 'react';
import * as PropTypes from 'prop-types';

const DistributionConfigurationForm = ({ csrfToken, readonly }) => (
    <div>
        Distribution Configuration Form goes here...
    </div>
);

DistributionConfigurationForm.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    // Pass this in for now while we have all descriptors in global state, otherwise retrieve this in this component
    readonly: PropTypes.bool
};

DistributionConfigurationForm.defaultProps = {
    readonly: false
};

export default DistributionConfigurationForm;
