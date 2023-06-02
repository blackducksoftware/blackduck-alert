import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import SearchFilter from 'common/component/table/SearchFilter';
import TableBody from 'common/component/table/TableBody';
import TableHeader from 'common/component/table/TableHeader';
import ToggleSwitch from 'common/component/input/ToggleSwitch';
import Pagination from 'common/component/navigation/Pagination';
import EmptyTableView from 'common/component/table/EmptyTableView';

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
        margin: ['10px', 0, '2px', 0]
    }
});

const Table = ({ columns, multiSelect, selected, onSelected, disableSelectOptions, tableData, handleSearchChange,
    searchBarPlaceholder, tableActions, onToggle, active, onSort, sortConfig, data, onPage, emptyTableConfig }) => {
    const classes = useStyles();

    return (
        <>
            { (tableActions || handleSearchChange || onToggle) && (
                <div className={classes.tableActions}>
                    {tableActions ? tableActions() : null}
                    { handleSearchChange && (
                        <SearchFilter
                            handleSearchChange={handleSearchChange}
                            searchBarPlaceholder={searchBarPlaceholder}
                        />
                    )}

                    {onToggle && (
                        <ToggleSwitch
                            active={active}
                            onToggle={onToggle}
                        />
                    )}
                </div>
            ) }

            { (!tableData || tableData.length === 0) && (
                <EmptyTableView emptyTableConfig={emptyTableConfig} />
            )}

            { tableData && tableData.length !== 0 && (
                <table className={classes.table}>
                    <TableHeader
                        columns={columns}
                        tableData={tableData}
                        multiSelect={multiSelect}
                        selected={selected}
                        onSelected={onSelected}
                        onSort={onSort}
                        sortConfig={sortConfig}
                        disableSelectOptions={disableSelectOptions}
                    />
                    <TableBody
                        columns={columns}
                        multiSelect={multiSelect}
                        tableData={tableData}
                        selected={selected}
                        onSelected={onSelected}
                        disableSelectOptions={disableSelectOptions}
                    />
                </table>
            )}

            { data?.totalPages > 1 && (
                <Pagination data={data} onPage={onPage} />
            )}
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
    disableSelectOptions: PropTypes.object,
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
    }),
    onPage: PropTypes.func,
    data: PropTypes.shape({
        totalPages: PropTypes.number
    }),
    emptyTableConfig: PropTypes.shape({
        message: PropTypes.string
    })
};

export default Table;
