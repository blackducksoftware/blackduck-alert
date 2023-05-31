import React, { useEffect, useMemo, useRef } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    multiSelectStyle: {
        textAlign: 'center',
        width: '45px'
    },
    inputStyle: {
        '&:disabled': {
            cursor: 'not-allowed'
        }
    }
});

const MultiSelectHeaderCell = ({ tableData, onSelected, selected, disableSelectOptions }) => {
    const classes = useStyles();
    const checkboxRef = useRef();

    const selectableData = tableData.filter((row) => !disableSelectOptions?.disabledItems.includes(row[disableSelectOptions.key]));

    // Return the ids of the rows if they are not included in the disableSelectOptions.disabledItems array
    const ids = useMemo(() => tableData?.filter((row) => (
        !disableSelectOptions?.disabledItems.includes(row[disableSelectOptions.key])
    )).map((item) => (
        // fieldName added for distribution tables
        item.id || item.fieldName
    )), [tableData]);

    useEffect(() => {
        if (selected?.length === 0) {
            checkboxRef.current.checked = false;
            checkboxRef.current.indeterminate = false;
        } else if (tableData?.length && tableData.length === selected?.length) {
            checkboxRef.current.checked = true;
            checkboxRef.current.indeterminate = false;
        } else {
            checkboxRef.current.indeterminate = true;
        }
    }, [selected, tableData]);

    function toggleAll() {
        if (selected?.length === 0) {
            onSelected(ids);
        } else {
            onSelected([]);
        }
    }

    return (
        <td className={classes.multiSelectStyle}>
            <input
                className={classes.inputStyle}
                type="checkbox"
                onChange={toggleAll}
                ref={checkboxRef}
                disabled={selectableData.length === 0}
            />
        </td>
    );
};

MultiSelectHeaderCell.propTypes = {
    tableData: PropTypes.arrayOf(PropTypes.object),
    onSelected: PropTypes.func,
    selected: PropTypes.arrayOf(PropTypes.string),
    disableSelectOptions: PropTypes.shape({
        disabledItems: PropTypes.array
    })
};

export default MultiSelectHeaderCell;
