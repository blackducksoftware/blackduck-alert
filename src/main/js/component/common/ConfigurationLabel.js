import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import * as IconUtility from 'util/iconUtility';

class ConfigurationLabel extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const {
            fontAwesomeIcon,
            configurationName,
            description,
            lastUpdated
        } = this.props;
        return (
            <div>
                <div className="d-inline-flex col-sm-4">
                    <h1 className="descriptorHeader">
                        <FontAwesomeIcon icon={IconUtility.createIconPath(fontAwesomeIcon)} className="alert-icon" size="lg" fixedWidth />
                        {configurationName}
                    </h1>
                </div>
                <div className="timeStampContainer">
                    {lastUpdated &&
                    <div>
                        <label className="text-right">Last Updated:</label>
                        <div className="d-inline-flex p-2">{lastUpdated}</div>
                    </div>
                    }
                </div>
                <div className="descriptorDescription">
                    {description}
                </div>
                <div className="descriptorBorder" />
            </div>
        );
    }
}

ConfigurationLabel.propTypes = {
    fontAwesomeIcon: PropTypes.string,
    configurationName: PropTypes.string.isRequired,
    description: PropTypes.string,
    lastUpdated: PropTypes.string
};

ConfigurationLabel.defaultProps = {
    fontAwesomeIcon: null,
    description: '',
    lastUpdated: null
};

export default ConfigurationLabel;
