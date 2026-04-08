import React from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    readOnlyField: {
        margin: '4px',
        paddingLeft: '8px',
        fontSize: '14px',
        fontWeight: 'bold'
    }
});

const ReadOnlyField = ({
    id, alt, description, errorName, errorValue, label, required, url, value
}) => {
    const classes = useStyles();
    const altValue = alt || url;
    const content = url ? <a alt={altValue} href={url}>{value}</a> : value;
    return (
        <LabeledField
            id={id}
            description={description}
            errorName={errorName}
            errorValue={errorValue}
            label={label}
            required={required}
        >
            <p className={classes.readOnlyField}>
                {content}
            </p>
        </LabeledField>
    );
};

ReadOnlyField.propTypes = {
    id: PropTypes.string,
    value: PropTypes.string,
    url: PropTypes.string,
    alt: PropTypes.string,
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    label: PropTypes.string.isRequired,
    required: PropTypes.bool
};

ReadOnlyField.defaultProps = {
    id: 'readOnlyFieldId',
    value: '',
    url: '',
    alt: '',
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT
};

export default ReadOnlyField;
