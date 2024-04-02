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

const MultiSelectHeaderCell = ({ tableData, onSelected, selected, disableSelectOptions, cellId }) => {
    const classes = useStyles();
    const checkboxRef = useRef();

    const selectableData = tableData.filter((row) => !disableSelectOptions?.disabledItems.includes(row[disableSelectOptions.key]));

    // Return the ids of the rows if they are not included in the disableSelectOptions.disabledItems array
    const ids = useMemo(() => tableData?.filter((row) => (
        !disableSelectOptions?.disabledItems.includes(row[disableSelectOptions.key])
    )).map((item) => (
        // jobId added for distribution tables
        // fieldName added for Advanced Jira Mapping Fields (in Distribution Config). This should eventually be removed and
        //  replaced in favor of UUIDs once a concrete implementation of the distribution endpoint is available.
        item.id || item.jobId || item.fieldName || item[cellId]
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
    }),
    cellId: PropTypes.string
};

export default MultiSelectHeaderCell;
