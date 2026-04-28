import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import SideNavButtonItem from 'common/component/navigation/SideNavButtonItem';
import SideNavSubmenu from 'common/component/navigation/SideNavSubmenu';

const useStyles = createUseStyles({
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
                ".     icon  caret"
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
                background: 'rgba(255, 255, 255, 0.1)',
                textDecoration: 'none',
                outlineOffset: -2
            },
            '& > svg': {
                margin: 'unset'
            }

        }
    }
});

const SideNavItem = ({ hasSubMenu, subMenuItems, id, href, icon, label, type, onClick }) => {
    const classes = useStyles();

    // If there is no submenu content, don't render anything.
    if (hasSubMenu && subMenuItems?.length === 0) {
        return null;
    }

    return (
        <li className={classes.navItem} id={id}>
            {hasSubMenu ? (
                <SideNavSubmenu
                    id={id}
                    icon={icon}
                    label={label}
                    subMenuItems={subMenuItems}
                />
            ) : (
                <SideNavButtonItem
                    id={id}
                    href={href}
                    icon={icon}
                    label={label}
                    type={type}
                    onClick={onClick}
                />
            )}
        </li>
    );
};

SideNavItem.propTypes = {
    href: PropTypes.string,
    icon: PropTypes.oneOfType([PropTypes.string, PropTypes.array]),
    label: PropTypes.string,
    onClick: PropTypes.func,
    type: PropTypes.string,
    hasSubMenu: PropTypes.bool,
    subMenuItems: PropTypes.array,
    id: PropTypes.string.isRequired
};

export default SideNavItem;
