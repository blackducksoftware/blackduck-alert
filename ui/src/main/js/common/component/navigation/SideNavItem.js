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

const SideNavItem = ({ hasSubMenu, ...props }) => {
    const classes = useStyles();

    const NavItem = hasSubMenu ? SideNavSubmenu : SideNavButtonItem;

    return (
        <li className={classes.navItem} role={null}>
            <NavItem {...props} />
        </li>
    );
}

SideNavItem.propTypes = {
    hasSubMenu: PropTypes.bool
};

export default SideNavItem;