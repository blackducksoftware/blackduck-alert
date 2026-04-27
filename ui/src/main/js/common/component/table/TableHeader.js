import React from 'react';
import classNames from 'classnames';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import MultiSelectHeaderCell from 'common/component/table/cell/MultiSelectHeaderCell';
import TableHeaderCell from 'common/component/table/cell/TableHeaderCell';

const useStyles = createUseStyles((theme) => ({
    tableHead: {
        backgroundColor: theme.colors.white.darkWhite,
        borderBottom: `solid 1px ${theme.colors.grey.lighterGrey}`,
        borderTop: `solid 1px ${theme.colors.grey.lighterGrey}`,
        height: '40px',

        '&:hover': {
            backgroundColor: theme.colors.white.darkWhite
        }
    },
    nonMultiSelect: {
        '& > tr > :first-child': {
            paddingLeft: '30px'
        }
    }
}));

const TableHeader = ({ columns, multiSelect, selected, onSelected, tableData, onSort, sortConfig, disableSelectOptions, cellId }) => {
    const classes = useStyles();

    const tableHeadClass = classNames(classes.tableHead, {
        [classes.nonMultiSelect]: !multiSelect
    });

    return (
        <thead className={tableHeadClass}>
            <tr>
                { multiSelect && (
                    <MultiSelectHeaderCell
                        selected={selected}
                        onSelected={onSelected}
                        tableData={tableData}
                        disableSelectOptions={disableSelectOptions}
                        cellId={cellId}
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
    disableSelectOptions: PropTypes.object,
    cellId: PropTypes.string
};

export default TableHeader;
