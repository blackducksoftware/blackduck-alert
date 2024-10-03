import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { NavLink } from 'react-router-dom';

const useStyles = createUseStyles({
    navLabel: {
        gridArea: 'label',
        opacity: 0.66,
        transition: 'opacity 0.3s ease-in-out'
    },
    icon: {
        gridArea: 'icon'
    }
});

const SideNavButtonItem = ({ href, icon, id, label, type = 'button', onClick }) => {
    const classes = useStyles();
    
    if (type === 'link') {
        return (
            <NavLink to={href}>
                <FontAwesomeIcon size="2x" className={classes.icon} icon={icon} fixedWidth />
                <span id={id} className={classes.navLabel}>{label}</span>
            </NavLink>
        )
    }
    
    // Default is button type
    return (
        <button onClick={onClick} type="button" name={id}>
            <FontAwesomeIcon size="2x" className={classes.icon} icon={icon} fixedWidth />
            <span id={id} className={classes.navLabel}>{label}</span>
        </button>
    )
}

SideNavButtonItem.propTypes = {
    id: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    href: PropTypes.string,
    onClick: PropTypes.func,
    type: PropTypes.string,
};

export default SideNavButtonItem;