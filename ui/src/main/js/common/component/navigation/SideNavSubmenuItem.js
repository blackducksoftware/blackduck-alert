import React from 'react';
import PropTypes from 'prop-types';

import { createUseStyles } from 'react-jss';

// import { MenuItem, MenuSeparator } from '../../../components/menu';
// import { IconPropTypes } from '../../../components/typography/Icon';

const useStyles = createUseStyles({
    item: {
        '& svg': {
            fontSize: '1.25em'
        }
    }
});

const SideNavSubmenuItem = ({ icon, label, href, showSeparator }) => {
    const classes = useStyles();
    return (
        <>
            <div>{label}</div>
            {/* <MenuItem icon={icon} href={href} className={classes.item}>
                {label}
            </MenuItem> */}
            {/* {showSeparator && <MenuSeparator />} */}
        </>
    );
}

// SideNavSubmenuItem.propTypes = {
//     icon: IconPropTypes,
//     label: PropTypes.string,
//     href: PropTypes.string,
//     showSeparator: PropTypes.bool
// };

export default SideNavSubmenuItem;