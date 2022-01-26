import React from 'react';
import PropTypes from 'prop-types';

const ConfigurationLabel = ({ configurationName, description, lastUpdated }) => (
    <div>
        <div className="d-inline-flex col-sm-4">
            <h1 className="descriptorHeader">
                {configurationName}
            </h1>
        </div>
        <div className="timeStampContainer">
            {lastUpdated
                    && (
                        <div>
                            <label className="text-right">Last Updated:</label>
                            <div className="d-inline-flex p-2">{lastUpdated}</div>
                        </div>
                    )}
        </div>
        <div className="descriptorDescription">
            {description}
        </div>
        <div className="descriptorBorder" />
    </div>
);

ConfigurationLabel.propTypes = {
    configurationName: PropTypes.string.isRequired,
    description: PropTypes.string,
    lastUpdated: PropTypes.string
};

ConfigurationLabel.defaultProps = {
    description: '',
    lastUpdated: null
};

export default ConfigurationLabel;
