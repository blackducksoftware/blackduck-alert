import React, { useCallback, useMemo } from 'react';
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

const MultiSelectCell = ({ selected, onSelected, data, disableSelectOptions }) => {
    const classes = useStyles();
    const { id } = data;
    const isSelected = useMemo(() => selected.includes(id), [selected, data]);
    const isDisabled = disableSelectOptions?.disabledItems.includes(data[disableSelectOptions.key]);

    const toggleSelect = useCallback((evt) => {
        if (evt.target.checked) {
            onSelected(selected.concat(id));
        } else {
            onSelected(selected.filter((item) => item !== id));
        }
    }, [onSelected, selected]);

    return (
        <td className={classes.multiSelectStyle}>
            <input
                // className={classes.inputStyle}
                type="checkbox"
                onChange={toggleSelect}
                checked={isSelected}
                disabled={isDisabled}
            />
        </td>
    );
};

MultiSelectCell.propTypes = {
    selected: PropTypes.arrayOf(PropTypes.string),
    onSelected: PropTypes.func,
    data: PropTypes.shape({
        id: PropTypes.string
    }),
    disableSelectOptions: PropTypes.shape({
        key: PropTypes.string,
        disabledItems: PropTypes.arrayOf(PropTypes.string)
    })
};

export default MultiSelectCell;
