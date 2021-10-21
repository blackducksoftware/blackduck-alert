import React from 'react';
import PropTypes from 'prop-types';
import ConfigurationLabel from 'common/ConfigurationLabel';

const CommonGlobalConfiguration = ({
    label, description, children, lastUpdated
}) => (
    <div className="mainBody">
        <ConfigurationLabel
            configurationName={label}
            description={description}
            lastUpdated={lastUpdated}
        />
        {children}
    </div>
);

CommonGlobalConfiguration.propTypes = {
    label: PropTypes.string.isRequired,
    description: PropTypes.string.isRequired,
    children: PropTypes.node.isRequired,
    lastUpdated: PropTypes.string
};

CommonGlobalConfiguration.defaultProps = {
    lastUpdated: null
};

export default CommonGlobalConfiguration;
