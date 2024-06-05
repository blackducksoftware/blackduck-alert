import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles({
    container: {
        width: 'fit-content',
        height: '100%',
        whiteSpace: 'nowrap'
    },
    label: {
        fontWeight: 'bold',
        marginRight: '4px'
    },
    icon: {
        marginRight: '2px'
    }
});

const LabelValuePair = ({ label, seperator, value, icon }) => {
    const classes = useStyles();
    const displayLabel = `${label}${seperator}`; 

    return (
        <div className={classes.container}>
            {icon && (
                <span className={classes.icon}>
                    <FontAwesomeIcon icon={icon} />
                </span>
            )}
            <span className={classes.label}>{displayLabel}</span>
            <span>{value}</span>
        </div>
    );
};

LabelValuePair.defaultProps = {
    seperator: ':'
};

LabelValuePair.propTypes = {
    label: PropTypes.string,
    value: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    seperator: PropTypes.string,
    icon: PropTypes.string
};

export default LabelValuePair;
