import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { NavLink } from 'react-router-dom';
import Dropdown from 'react-bootstrap/Dropdown';

const useStyles = createUseStyles({
    navLabel: {
        gridArea: 'label',
        opacity: 0.66,
        transition: 'opacity 0.3s ease-in-out'
    },
    icon: {
        gridArea: 'icon',
        margin: '0 auto'
    },
    caret: {
        gridArea: 'caret',
        justifySelf: 'start',
        margin: 'auto 0'
    },
    navItem: {
        display: 'grid',
        gridTemplateAreas: `
            ". icon caret"
            "label label label"
        `,
        gridTemplateColumns: '16px 1fr 16px',
        color: 'white',
        backgroundColor: 'transparent !important',
        border: 'none',
        fontSize: '1em',
        fontWeight: 400,
        transition: 'background-color 0.3s ease-in-out',
        padding: '12px 0',
        width: '100%',
        rowGap: '6px',
        '&:hover, &:focus, &:active': {
            color: 'white',
            background: '#313944 !important',
            textDecoration: 'none'
        },
        '&::after': {
            display: 'none !important'
        }
    },
    navMenu: {
        backgroundColor: 'black',
        color: 'white',
        minWidth: '200px',
        border: '1px solid #dddddd',
        borderRadius: '3px',
        boxShadow: '0 8px 16px rgba(0, 0, 0, 0.175)',
        fontSize: '13px'
    },
    navMenuItem: {
        color: 'white',
        padding: ['8px', 0, '8px', '24px'],
        display: 'block',
        width: '100%',
        fontWeight: 400,
        whiteSpace: 'nowrap',
        textDecoration: 'none',
        '&:hover': {
            background: '#313944',
            color: 'white',
            textDecoration: 'none'
        }
    }
});

const SideNavSubmenu = ({ icon, id, label, subMenuItems }) => {
    const classes = useStyles();

    return (
        <Dropdown drop="end">
            <Dropdown.Toggle className={classes.navItem}>
                <FontAwesomeIcon size="2x" className={classes.icon} icon={icon} fixedWidth />
                <span id={id} className={classes.navLabel}>{label}</span>
                <FontAwesomeIcon size="1x" className={classes.caret} icon="caret-right" fixedWidth />
            </Dropdown.Toggle>
            <Dropdown.Menu className={classes.navMenu}>
                {subMenuItems.map((item) => {
                    if (!item.showOption) {
                        return null;
                    }
                    return (
                        <Dropdown.Item as={NavLink} to={item.href} className={classes.navMenuItem} key={item.id}>
                            {item.label}
                        </Dropdown.Item>
                    );
                })}
            </Dropdown.Menu>
        </Dropdown>
    );
};

SideNavSubmenu.propTypes = {
    icon: PropTypes.string,
    id: PropTypes.string,
    label: PropTypes.string,
    subMenuItems: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string,
        label: PropTypes.string,
        href: PropTypes.string,
        showOption: PropTypes.bool
    }))
};

export default SideNavSubmenu;
