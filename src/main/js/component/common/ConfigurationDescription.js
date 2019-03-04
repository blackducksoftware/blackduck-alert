import React, { Component } from 'react';
import PropTypes from 'prop-types';

class ConfigurationDescription extends Component {
    render() {
        const { description, documentationLink } = this.props;
        return (
            <div>
                <div>
                    <div className="col-sm-3 d-inline-flex" />
                    <p className="d-inline-flex col-sm-8">{description}</p>
                </div>
                <div>
                    <div className="col-sm-3 d-inline-flex" />
                    <div className="d-inline-flex col-sm-8">
                        <p className="d-inline-flex">For more information, please refer to the </p>
                        <a className="d-inline-flex" target="_blank" href={documentationLink}>documentation.</a>
                    </div>
                </div>
            </div>);
    }
}

ConfigurationDescription.propTypes = {
    description: PropTypes.string.isRequired,
    documentationLink: PropTypes.string
};

ConfigurationDescription.defaultProps = {
    documentationLink: ''
}

export default ConfigurationDescription;
