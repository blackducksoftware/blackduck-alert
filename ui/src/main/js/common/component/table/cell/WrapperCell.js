import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    wrapperCell: {
        padding: '8px',
        textAlign: 'left',
        '&:empty::after': {
            content: '"\\00a0"'
        }
    },
    right: {
        textAlign: 'right',
        paddingRight: '35px'
    },
    center: {
        textAlign: 'center'
    }
});

const WrapperCell = ({ children, settings }) => {
    const classes = useStyles();
    const cellStyle = classNames(classes.wrapperCell, {
        [classes.right]: settings?.alignment === 'right',
        [classes.center]: settings?.alignment === 'center'
    });

    return (
        <td className={cellStyle}>
            {children}
        </td>
    );
};

WrapperCell.propTypes = {
    settings: PropTypes.object,
    children: PropTypes.any
};

export default WrapperCell;
