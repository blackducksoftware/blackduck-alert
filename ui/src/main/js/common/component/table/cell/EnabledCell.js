import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faCircle } from '@fortawesome/free-solid-svg-icons';
import classNames from 'classnames';

const useStyles = createUseStyles((theme) => ({
    container: {
        display: 'flex',
        alignItems: 'center',
        columnGap: '8px',
        margin: 'auto',
        fontSize: '14px',
        width: 'fit-content'
    },
    textClass: {
        padding: 0,
        margin: 0
    },
    disabledText: {
        color: theme.colors.grey.default
    },
    indicator: {
        display: 'flex',
        alignItems: 'center'
    },
    enabledIndicator: {
        color: theme.colors.green.lightGreen
    },
    disabledIndicator: {
        color: theme.colors.grey.lightGrey
    }
}));

const EnabledCell = ({ data }) => {
    const classes = useStyles();
    const { enabled } = data;

    const indicatorClass = classNames(classes.indicator, {
        [classes.enabledIndicator]: enabled,
        [classes.disabledIndicator]: !enabled
    });

    const textClass = classNames(classes.textClass, {
        [classes.disabledText]: !enabled
    });

    return (
        <div className={classes.container}>
            <FontAwesomeIcon icon={faCircle} className={indicatorClass} fontSize="8px" />
            <p className={textClass}>
                {enabled ? 'Enabled' : 'Disabled'}
            </p>
        </div>
    );
};

EnabledCell.propTypes = {
    data: PropTypes.shape({
        enabled: PropTypes.bool
    })
};

export default EnabledCell;
