import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    headerCell: {
        fontWeight: 'bold',
        '& > button': {
            background: 'none',
            border: 0,
            fontWeight: 'bold',
            minHeight: '34px',
            textAlign: 'left',
            width: '100%'
        }
    },
    right: {
        textAlign: 'right',
        paddingRight: '15px'
    },
    center: {
        textAlign: 'center'
    },
    sortableCell: {
        '&:hover': {
            borderBottom: [1, 'solid', '#787884']
        }
    }
});


const TableHeaderCell = ({ label, sortable, settings }) => {
    const classes = useStyles();
    const cellStyle = classNames(classes.headerCell, {
        [classes.sortableCell]: sortable,
        [classes.right]: settings?.alignment === 'right',
        [classes.center]: settings?.alignment === 'center'
    });

    return (
        <th className={cellStyle} >
            { sortable ? (
                <button>
                    {label}
                </button>
            ) : <>{label}</> }
            
        </th>
    );
};

TableHeaderCell.propTypes = {
    sortable: PropTypes.bool,
    label: PropTypes.string,
    settings:PropTypes.object
};

export default TableHeaderCell;