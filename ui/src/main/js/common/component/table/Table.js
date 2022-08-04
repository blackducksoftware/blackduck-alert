import React from 'react';
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
    searchBarPlaceholder, tableActions, onToggle, active,  }) => {
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

export default Table;