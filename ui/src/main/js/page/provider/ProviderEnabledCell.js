import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

const useStyles = createUseStyles({
    enabled: {
        color: 'green'
    },
    disabled: {
        color: 'red'
    }
});

const ProviderEnabledCell = ({ data }) => {
    const classes = useStyles();
    const { enabled } = data;

    return (
        <div className={enabled ? classes.enabled : classes.disabled}>
            <FontAwesomeIcon icon={enabled ? 'check' : 'times'} />
        </div>

    );
};

ProviderEnabledCell.propTypes = {
    data: PropTypes.shape({
        enabled: PropTypes.bool
    })
};

export default ProviderEnabledCell;
