import React from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import SearchFilter from 'common/component/table/SearchFilter';
import TableBody from 'common/component/table/TableBody';
import TableHeader from 'common/component/table/TableHeader';
import ToggleSwitch from 'common/component/input/ToggleSwitch';
import EmptyTableView from 'common/component/table/EmptyTableView';
import TableFooter from 'common/component/table/TableFooter';
import TableSkeleton from 'common/component/table/TableSkeleton';

const useStyles = createUseStyles(theme => ({
    tableContainer: {
        border: 'solid 1px #ddd',
        borderRadius: '8px',
        backgroundColor: theme.colors.white.default,
        boxShadow: `0 1px 3px 0 ${theme.colors.borderColor}, 0 1px 2px -1px ${theme.colors.borderColor}`
    },
    table: {
        width: '100%',
        border: 0,
        '& tr:hover': {
            backgroundColor: '#F8F8F9'
        }
    },
    tableActions: {
        display: 'flex',
        alignItems: 'center',
        backgroundColor: theme.colors.white.default,
        margin: ['14px', '20px']
    }
}));

const Table = ({
    columns, multiSelect, selected, onSelected, disableSelectOptions, tableData, handleSearchChange,
    searchBarPlaceholder, tableActions, onToggle, active, onSort, sortConfig, data, onPage, emptyTableConfig,
    defaultSearchValue, onPageSize, showPageSize, pageSize, cellId, isLoading
}) => {
    const classes = useStyles();

    return (
        <div className={classes.tableContainer}>
            {(tableActions || handleSearchChange || onToggle) && (
                <div className={classes.tableActions}>
                    {tableActions ? tableActions() : null}
                    {handleSearchChange && (
                        <SearchFilter
                            handleSearchChange={handleSearchChange}
                            searchBarPlaceholder={searchBarPlaceholder}
                            defaultSearchValue={defaultSearchValue}
                            isDisabled={isLoading}
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

            { isLoading && (
                <TableSkeleton />
            )}

            { (!isLoading && !tableData || tableData?.length === 0) && (
                <EmptyTableView emptyTableConfig={emptyTableConfig} />
            )}

            { (!isLoading && tableData && tableData.length !== 0) && (
                <>
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
                            cellId={cellId}
                        />
                        <TableBody
                            columns={columns}
                            multiSelect={multiSelect}
                            tableData={tableData}
                            selected={selected}
                            onSelected={onSelected}
                            disableSelectOptions={disableSelectOptions}
                            cellId={cellId}
                        />
                    </table>
                    
                    {showPageSize && (
                        <TableFooter data={data} onPage={onPage} onPageSize={onPageSize} showPageSize={showPageSize} pageSize={pageSize} />
                    )}
                </>
            )}
        </div>
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
    onPageSize: PropTypes.func,
    showPageSize: PropTypes.bool,
    data: PropTypes.object,
    emptyTableConfig: PropTypes.shape({
        message: PropTypes.string
    }),
    defaultSearchValue: PropTypes.string,
    pageSize: PropTypes.oneOfType([PropTypes.string, PropTypes.number]),
    cellId: PropTypes.string,
    isLoading: PropTypes.bool
};

export default Table;
