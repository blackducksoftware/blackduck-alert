import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import * as IconUtility from 'util/iconUtility';

class ConfigurationLabel extends Component {
    constructor(props) {
        super(props);
    }

    render() {
        const { configurationName, description } = this.props;

        return (
            <div>
                <div className="d-inline-flex col-sm-4">
                    <h1 className="descriptorHeader">
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
    configurationName: PropTypes.string.isRequired,
    description: PropTypes.string
};

ConfigurationLabel.defaultProps = {
    description: ''
};

export default ConfigurationLabel;
