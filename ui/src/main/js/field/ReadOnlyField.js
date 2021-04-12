import React from 'react';
import PropTypes from 'prop-types';
import LabeledField from 'field/LabeledField';

const ReadOnlyField = (props) => {
    const createUrl = () => {
        const { value, url, alt } = props;
        if (url) {
            const altValue = alt || url;
            return (<a alt={altValue} href={url}>{value}</a>);
        }

        return value;
    };

    return (
        <LabeledField {...props}>
            <div className="d-inline-flex p-2 col-sm-8"><p className="form-control-static">{createUrl()}</p></div>
        </LabeledField>
    );
};

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
