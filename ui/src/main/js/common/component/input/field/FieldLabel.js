import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import classNames from 'classnames';

const useStyles = createUseStyles({
    fieldLabel: {
        cursor: 'pointer',
        display: 'block',
        fontWeight: 'bold',
        marginBottom: 0,
        padding: ['4px', 0]
    },
    fieldLabelRequired: {
        '&::after': {
            content: '"*"',
            color: 'red',
            fontSize: '0.8em',
            marginLeft: '4px'
        }
    },
    fieldDescription: {
        margin: [0, 0, '8px']
    },
    unlinked: {
        cursor: 'default'
    }
});

const FieldLabel = ({ label, description, fieldId, isRequired  }) => {
    const classes = useStyles();

    // Build class names for FieldLabel
    const labelClass = classNames(classes.fieldLabel, {
        [classes.fieldLabelRequired]: isRequired,
        [classes.unlinked]: !fieldId
    });

    return (
        <div>
            <label className={labelClass} htmlFor={fieldId}>
                {label}
            </label>
            { description && (
                <div className={classes.fieldDescription}>{description}</div>
            )}
        </div>
    );
};

FieldLabel.propTypes = {
    label: PropTypes.string,
    description: PropTypes.string,
    fieldId: PropTypes.string,
    isRequired: PropTypes.bool
};

export default FieldLabel;
