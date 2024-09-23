import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import SideNavButtonItem from 'common/component/navigation/SideNavButtonItem';
import SideNavSubmenu from 'common/component/navigation/SideNavSubmenu';

const useStyles = createUseStyles(theme => ({
    navItem: {
        color: 'white',
        fontSize: '0.85em',
        fontWeight: 400,
        transition: 'background-color 0.3s ease-in-out',

        '& > button, & > a': {
            alignItems: 'center',
            color: 'white',
            display: 'grid',
            gridTemplateAreas: `
                ". icon caret"
                "label label label"
            `,
            gridTemplateColumns: '16px 1fr 16px',
            justifyItems: 'center',
            padding: ['12px', 0],
            rowGap: '6px',
            textDecoration: 'none',
            userSelect: 'none',
            width: '100%',
            border: 'none',
            backgroundColor: 'transparent',
            '&:hover, &:focus': {
                color: 'white',
                background: '#313944',
                textDecoration: 'none',
                outlineOffset: -2
            },
            '& > svg': {
                margin: 'unset'
            }

        }
    }
}));

const SideNavItem = ({ ...props }) => {
    const classes = useStyles();
    const { hasSubMenu, subMenuItems } = props;

    // If there is no submenu content, don't render anything.
    if (hasSubMenu && subMenuItems?.length === 0) {
        return null;
    }

    const NavItem = hasSubMenu ? SideNavSubmenu : SideNavButtonItem;

    return (
        <li className={classes.navItem} role={null}>
            <NavItem {...props} />
        </li>
    );
}

SideNavItem.propTypes = {
    hasSubMenu: PropTypes.bool,
    subMenuItems: PropTypes.array
};

export default SideNavItem;