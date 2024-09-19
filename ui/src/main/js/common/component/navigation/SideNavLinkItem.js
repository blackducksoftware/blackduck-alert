import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

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

const SideNavLinkItem = ({ href, icon, id, label }) => {
    const classes = useStyles();
    return (
        <a href={href} tabIndex={0}>
            <FontAwesomeIcon size="2x" className={classes.icon} icon={icon} fixedWidth />
            <span id={id} className={classes.navLabel}>{label}</span>
        </a>
    );
}

SideNavLinkItem.propTypes = {
    id: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    href: PropTypes.string.isRequired
};

export default SideNavLinkItem;