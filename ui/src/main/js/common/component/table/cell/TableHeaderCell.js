import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

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
    cell: {
        padding: ['1px', '6px']
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
    },
    buttonContainer: {
        display: 'flex',
        alignItems: 'center',
        columnGap: '3px',
        '&:focus': {
            border: 'none',
            outline: 'none'
        }
    }
});

const TableHeaderCell = ({ label, sortable, settings, onSort, name, sortConfig }) => {
    const classes = useStyles();
    const cellStyle = classNames(classes.headerCell, {
        [classes.sortableCell]: sortable,
        [classes.right]: settings?.alignment === 'right',
        [classes.center]: settings?.alignment === 'center'
    });

    function getSortIcon(direction) {
        return direction === 'ASC' ? 'sort-up' : 'sort-down';
    }

    return (
        <th className={cellStyle}>
            { sortable ? (
                <button type="button" onClick={() => onSort(name)} className={classes.buttonContainer}>
                    {label}
                    { sortConfig && sortConfig?.name === name ? (
                        <FontAwesomeIcon icon={getSortIcon(sortConfig.direction)} />
                    ) : <FontAwesomeIcon icon="sort" /> }
                </button>
            ) : <div className={classes.cell}>{label}</div> }
        </th>
    );
};

TableHeaderCell.propTypes = {
    sortable: PropTypes.bool,
    label: PropTypes.string,
    settings: PropTypes.object,
    onSort: PropTypes.func,
    name: PropTypes.string,
    sortConfig: PropTypes.shape({
        name: PropTypes.string,
        direction: PropTypes.string
    })
};

export default TableHeaderCell;
