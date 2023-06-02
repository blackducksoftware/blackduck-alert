import React from 'react';
import PropTypes from 'prop-types';
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

const TableHeader = ({ columns, multiSelect, selected, onSelected, tableData, onSort, sortConfig, disableSelectOptions }) => {
    const classes = useStyles();

    return (
        <thead className={classes.tableHead}>
            <tr>
                { multiSelect && (
                    <MultiSelectHeaderCell
                        selected={selected}
                        onSelected={onSelected}
                        tableData={tableData}
                        disableSelectOptions={disableSelectOptions}
                    />
                )}

                { columns.map((column) => (
                    <TableHeaderCell
                        key={column.key}
                        label={column.label}
                        sortable={column.sortable}
                        settings={column.settings}
                        onSort={onSort}
                        name={column.key}
                        sortConfig={sortConfig}
                    />
                ))}
            </tr>
        </thead>
    );
};

TableHeader.propTypes = {
    columns: PropTypes.arrayOf(PropTypes.shape({
        key: PropTypes.string,
        label: PropTypes.string,
        sortable: PropTypes.bool
    })),
    multiSelect: PropTypes.bool,
    selected: PropTypes.array,
    onSelected: PropTypes.func,
    tableData: PropTypes.arrayOf(PropTypes.object),
    onSort: PropTypes.func,
    sortConfig: PropTypes.shape({
        name: PropTypes.string,
        direction: PropTypes.string
    }),
    disableSelectOptions: PropTypes.object
};

export default TableHeader;
