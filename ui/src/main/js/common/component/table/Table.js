import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import SearchFilter from 'common/component/table/SearchFilter';
import TableBody from 'common/component/table/TableBody';
import TableHeader from 'common/component/table/TableHeader';
import ToggleSwitch from 'common/component/input/ToggleSwitch';

const useStyles = createUseStyles({
    table: {
        width: '100%',
        border: 0,
        '& tr:nth-child(even)': {
            backgroundColor: '#f0f0fb'
        }
    },
    tableActions: {
        display: 'flex',
        alignItems: 'center',
        margin: ['10px', 0, '5px', 0]
    }
});


const Table = ({ columns, multiSelect, selected, onSelected, tableData, handleSearchChange, 
    searchBarPlaceholder, tableActions, onToggle, active, onSort, sortConfig }) => {
    const classes = useStyles();

    return (
        <>
            <div className={classes.tableActions}>
                {tableActions ? tableActions() : null}
                <SearchFilter 
                    handleSearchChange={handleSearchChange}
                    searchBarPlaceholder={searchBarPlaceholder}
                />
                <ToggleSwitch 
                    active={active}
                    onToggle={onToggle} 
                />
            </div>
            <table className={classes.table}>
                <TableHeader 
                    columns={columns}
                    tableData={tableData} 
                    multiSelect={multiSelect} 
                    selected={selected}
                    onSelected={onSelected}
                    onSort={onSort}
                    sortConfig={sortConfig}
                />
                <TableBody 
                    columns={columns} 
                    multiSelect={multiSelect} 
                    tableData={tableData} 
                    selected={selected}
                    onSelected={onSelected}
                />
            </table>
        </>
    );
};

Table.propTypes = {
    columns: PropTypes.arrayOf(PropTypes.shape({
        key: PropTypes.string,
        label: PropTypes.string,
        sortable: PropTypes.bool
    })),
    multiSelect: PropTypes.bool,
    selected: PropTypes.arrayOf(PropTypes.string),
    onSelected: PropTypes.func,
    tableData: PropTypes.arrayOf(PropTypes.object),
    handleSearchChange: PropTypes.func, 
    searchBarPlaceholder: PropTypes.string,
    tableActions: PropTypes.func,
    onToggle: PropTypes.func,
    active: PropTypes.bool,
    onSort: PropTypes.func,
    sortConfig: PropTypes.shape({
        name: PropTypes.string,
        direction: PropTypes.string
    })
};

export default Table;