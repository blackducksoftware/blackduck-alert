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

const SideNavItemGroup = ({ id, label, showSeparator, groupItems }) => {
    const classes = useStyles();
    // const groupItems = useSelector(state => state.ui.sidenav.groups[id]) || [];

    if (!groupItems.length) {
        return null;
    }
    
    return (
        <>
            {/* <BaseMenuItem className={classes.groupLabel}>
                <div role="separator">{label}</div>
            </BaseMenuItem> */}
            {groupItems.map(item => (
                <SideNavSubmenuItem key={item.id} {...item} />
            ))}
            {/* {showSeparator && <MenuSeparator />} */}
        </>
    );
}

// SideNavItemGroup.propTypes = {
//     id: PropTypes.string.isRequired,
//     label: PropTypes.string.isRequired,
//     showSeparator: PropTypes.bool
// };

export default SideNavItemGroup;