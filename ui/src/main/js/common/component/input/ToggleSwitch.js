import React, { useMemo } from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    switchContainer: {
        display: 'flex',
        flexDirection: 'column',
        marginLeft: '10px',
        width: '60px'
    },
    switch: {
        position: 'relative',
        display: 'inline-block',
        margin: [0, 0, 0, '8px']
    },
    switchInput: {
        display: 'none'
    },
    switchLabel: {
        display: 'block',
        width: '40px',
        height: '15px',
        textIndent: '-150%',
        clip: 'rect(0 0 0 0)',
        color: 'transparent',
        userSelect: 'none',
        '&::before, &::after': {
            content: '""',
            display: 'block',
            position: 'absolute',
            cursor: 'pointer'
        },
        '&::before': {
            width: '100%',
            height: '100%',
            backgroundColor: '#dedede',
            borderRadius: '9999em',
            transition: 'background-color 0.25s ease'
        },
        '&::after': {
            top: 0,
            left: 0,
            width: '17px',
            height: '15px',
            backgroundColor: '#fff',
            borderRadius: '50%',
            boxShadow: '0 0 2px rgba(0, 0, 0, 0.45)',
            transition: 'left 0.25s ease'
        }
    },
    toggleSwitchActive: {
        '&::before': {
            backgroundColor: '#89c12d'
        },
        '&::after': {
            left: '24px'
        }
    }

});

const ToggleSwitch = ({ onToggle, active }) => {
    const classes = useStyles();

    const switchClasses = useMemo(() => classNames(classes.switchLabel, {
        [classes.toggleSwitchActive]: active
    }), [active]);

    return (
        <div className={classes.switchContainer}>
            <span style={{ fontSize: '10px' }}>
                Auto-Refresh
            </span>
            <span>
                <label className={classes.switch}>
                    <input className={classes.switchInput} type="checkbox" checked={active} onChange={onToggle} />
                    <span className={switchClasses} />
                </label>
            </span>
        </div>
    );
};

ToggleSwitch.propTypes = {
    onToggle: PropTypes.func,
    active: PropTypes.bool
};

export default ToggleSwitch;
