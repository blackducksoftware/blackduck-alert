import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import * as IconUtility from 'util/iconUtility';

class ConfigurationLabel extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const { fontAwesomeIcon, configurationName, description } = this.props;

        return (
            <div>
                <div className="d-inline-flex col-sm-4">
                    <h1 className="descriptorHeader">
                        <FontAwesomeIcon icon={IconUtility.createIconPath(fontAwesomeIcon)} className="alert-icon" size="lg" fixedWidth />
                        {configurationName}
                    </h1>
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
    description: PropTypes.string
};

ConfigurationLabel.defaultProps = {
    fontAwesomeIcon: null,
    description: ''
};

export default ConfigurationLabel;
