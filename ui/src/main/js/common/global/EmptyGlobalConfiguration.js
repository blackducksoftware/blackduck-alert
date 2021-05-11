import React from 'react';
import * as PropTypes from 'prop-types';
import CommonGlobalConfiguration from 'common/global/CommonGlobalConfiguration';

const EmptyGlobalConfiguration = ({ label, description }) => (
    <CommonGlobalConfiguration label={label} description={description}>
        <div className="form-horizontal">
            There is no global configuration required. The configuration is handled in the distribution jobs.
        </div>
    </CommonGlobalConfiguration>
);

EmptyGlobalConfiguration.propTypes = {
    label: PropTypes.string.isRequired,
    description: PropTypes.string.isRequired
};

export default EmptyGlobalConfiguration;
