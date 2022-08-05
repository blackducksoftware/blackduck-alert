import React from 'react';
import { createUseStyles } from 'react-jss';
import MultiSelectHeaderCell from 'common/component/table/cell/MultiSelectHeaderCell';
import TableHeaderCell from 'common/component/table/cell/TableHeaderCell';

const useStyles = createUseStyles({
    tableHead: {
        backgroundColor: '#e7e7f0',
        borderBottom: [1, 'solid', '#c8c8dd'],
        borderTop: [1, 'solid', '#c8c8dd'],
        height: '36px'
    }
});

const TableHeader = ({ columns, multiSelect, selected, onSelected, tableData  }) => {
    const classes = useStyles();

    return (
        <thead className={classes.tableHead}>
            <tr>
                { multiSelect ? (
                    <MultiSelectHeaderCell 
                        selected={selected}
                        onSelected={onSelected}
                        tableData={tableData}
                    />
                ) : null }
                
                { columns.map(column => (
                    <TableHeaderCell 
                        key={column.key} 
                        label={column.label} 
                        sortable={column.sortable} 
                        settings={column.settings} 
                    />
                ))}
            </tr>
        </thead>
    );
};

export default TableHeader;