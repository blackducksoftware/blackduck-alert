import React from 'react';
import PropTypes from 'prop-types';
import classNames from 'classnames';
import MultiSelectCell from 'common/component/table/cell/MultiSelectCell';
import WrapperCell from 'common/component/table/cell/WrapperCell';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles(theme => ({
    tableRow: {
        height: '52px',
        borderBottom: `1px solid ${theme.colors.grey.lighterGrey}`,
    },
    nonMultiSelect: {
        '& > :first-child': {
            paddingLeft: '35px'
        }
    }
}));

const TableBody = ({ columns, multiSelect, tableData, selected, onSelected, disableSelectOptions, cellId }) => {
    const classes = useStyles();

    const tableRowClass = classNames(classes.tableRow, {
        [classes.nonMultiSelect]: !multiSelect
    });

    return (
        <tbody>
            {tableData?.map((rowData, rowIndex) => (
                <tr key={`${rowIndex}-table-row`} className={tableRowClass}>
                    {multiSelect && (
                        <MultiSelectCell
                            data={rowData}
                            selected={selected}
                            onSelected={onSelected}
                            disableSelectOptions={disableSelectOptions}
                            cellId={cellId}
                        />
                    )}

                    { columns.map((col, colIndex) => {
                        const columnKey = `${col.key}-${rowIndex}-${colIndex}`;
                        if (col.customCell) {
                            const CustomCell = col.customCell;

                            return (
                                <WrapperCell key={columnKey} settings={col.settings}>
                                    <CustomCell id={col.key} data={rowData} settings={col.settings} customCallback={col.customCallback} />
                                </WrapperCell>
                            );
                        }

                        return (
                            <WrapperCell key={columnKey} datakey={col.key}>
                                {rowData[col.key] ? rowData[col.key] : '\u2014'}
                            </WrapperCell>
                        );
                    })}
                </tr>
            ))}
        </tbody>
    );
};

TableBody.propTypes = {
    columns: PropTypes.arrayOf(PropTypes.shape({
        key: PropTypes.string,
        label: PropTypes.string,
        sortable: PropTypes.bool
    })),
    multiSelect: PropTypes.bool,
    selected: PropTypes.arrayOf(PropTypes.string),
    onSelected: PropTypes.func,
    tableData: PropTypes.arrayOf(PropTypes.object),
    disableSelectOptions: PropTypes.object,
    cellId: PropTypes.string
};

export default TableBody;
