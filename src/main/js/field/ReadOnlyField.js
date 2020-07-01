import React, { Component } from 'react';
import PropTypes from 'prop-types';
import LabeledField from 'field/LabeledField';


class ReadOnlyField extends Component {
    constructor(props) {
        super(props);

        this.createUrl = this.createUrl.bind(this);
    }

    createUrl() {
        const { value, url, alt } = this.props;
        if (url) {
            const altValue = alt || url;
            return (<a alt={altValue} href={url}>{value}</a>);
        }

        return value;
    }

    render() {
        const field = (<div className="d-inline-flex p-2 col-sm-8"><p className="form-control-static">{this.createUrl()}</p></div>);
        return (
            <LabeledField field={field} {...this.props} />
        );
    }
}

ReadOnlyField.propTypes = {
    id: PropTypes.string,
    value: PropTypes.string,
    url: PropTypes.string,
    alt: PropTypes.string
};

ReadOnlyField.defaultProps = {
    id: 'readOnlyFieldId',
    value: '',
    url: '',
    alt: ''
};

export default ReadOnlyField;
