import React, { useEffect, useMemo, useRef } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles({
    multiSelectStyle: {
        textAlign: 'center',
        width: '45px'
    },
    inputStyle: {
        cursor: 'pointer'
    }
});

const MultiSelectHeaderCell = ({ tableData, onSelected, selected }) => {
    const classes = useStyles();
    const checkboxRef = useRef();

    const ids = useMemo(() => tableData?.map((item) => {
        return item.id;
    }), [tableData]);

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

    function toggleAll(evt) {
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
            />
        </td>
    );
};

MultiSelectHeaderCell.propTypes = {
    tableData: PropTypes.arrayOf(PropTypes.object),
    onSelected: PropTypes.func,
    selected: PropTypes.arrayOf(PropTypes.string)
};

export default MultiSelectHeaderCell;