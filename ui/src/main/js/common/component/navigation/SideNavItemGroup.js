import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { useSelector } from 'react-redux';
// import { BaseMenuItem, MenuSeparator } from '../../../components/menu';
import SideNavSubmenuItem from 'common/component/navigation/SideNavSubmenuItem';

const useStyles = createUseStyles(theme => ({
    groupLabel: {
        color: '#aaaaaa',
        textTransform: 'uppercase',
        fontSize: '0.9em', 
        '& > div': {
            paddingBottom: '4px'
        }
    }
}));

const SideNavItemGroup = ({ groupItems }) => {
    const classes = useStyles();

    if (!groupItems.length) {
        return null;
    }
    
    return (
        <>
            {groupItems.map(item => (
                <SideNavSubmenuItem key={item.id} {...item} />
            ))}
        </>
    );
}

SideNavItemGroup.propTypes = {
    groupItems: PropTypes.shape({
        id: PropTypes.string,
        label: PropTypes.string,
        href: PropTypes.string
    })
};

export default SideNavItemGroup;