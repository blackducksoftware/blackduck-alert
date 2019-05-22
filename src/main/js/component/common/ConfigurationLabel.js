import React, { Component } from 'react';
import PropTypes from 'prop-types';
import Tooltip from 'react-bootstrap/Tooltip';
import Overlay from 'react-bootstrap/Overlay';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

class ConfigurationLabel extends Component {
    constructor(props) {
        super(props);

        this.state = {
            showDescription: false
        };
    }

    render() {
        const { showDescription } = this.state;
        const { fontAwesomeIcon, configurationName, description } = this.props;

        let descriptionField = null;
        if (description) {
            descriptionField = (
                <div className="d-inline-flex">
                    <span
                        className="configurationDescriptionIcon"
                        onClick={() => this.setState({ showDescription: !showDescription })}
                        ref={(icon) => {
                            this.target = icon;
                        }}
                    >
                        <FontAwesomeIcon icon="question-circle" className="alert-icon" size="lg" />
                    </span>
                    <Overlay
                        rootClose
                        placement="bottom"
                        show={showDescription}
                        onHide={() => this.setState({ showDescription: false })}
                        target={() => this.target}
                        container={this}
                    >
                        <Tooltip id="description-tooltip">
                            {description}
                        </Tooltip>
                    </Overlay>
                </div>
            );
        }

        return (
            <div className="d-inline-flex col-sm-4">
                <h1>
                    <FontAwesomeIcon icon={fontAwesomeIcon} className="alert-icon" size="lg" fixedWidth />
                    {configurationName}
                    {descriptionField}
                </h1>
            </div>);
    }
}

ConfigurationLabel.propTypes = {
    fontAwesomeIcon: PropTypes.string.isRequired,
    configurationName: PropTypes.string.isRequired,
    description: PropTypes.string

};

ConfigurationLabel.defaultProps = {
    description: ''
};

export default ConfigurationLabel;
