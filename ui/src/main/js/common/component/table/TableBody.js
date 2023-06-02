import React from 'react';
import PropTypes from 'prop-types';
import MultiSelectCell from 'common/component/table/cell/MultiSelectCell';
import WrapperCell from 'common/component/table/cell/WrapperCell';

const TableBody = ({ columns, multiSelect, tableData, selected, onSelected, disableSelectOptions }) => (
    <tbody>
        { tableData?.map((rowData, rowIndex) => (
            <tr key={`${rowIndex}-table-row`}>
                { multiSelect && (
                    <MultiSelectCell
                        data={rowData}
                        selected={selected}
                        onSelected={onSelected}
                        disableSelectOptions={disableSelectOptions}
                    />
                )}

                { columns.map((col, colIndex) => {
                    const columnKey = `${col.key}-${rowIndex}-${colIndex}`;
                    if (col.customCell) {
                        const CustomCell = col.customCell;

                        return (
                            <WrapperCell key={columnKey} settings={col.settings}>
                                <CustomCell data={rowData} settings={col.settings} customCallback={col.customCallback} />
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
    disableSelectOptions: PropTypes.object
};

export default TableBody;
