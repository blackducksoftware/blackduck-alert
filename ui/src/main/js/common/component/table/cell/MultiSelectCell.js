import React, { useCallback, useMemo } from 'react';
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

const MultiSelectCell = ({ selected, onSelected, data, disableSelectOptions }) => {
    const classes = useStyles();
    // jobId added for distribution tables
    const { id, jobId } = data;
    const identifier = id || jobId;
    const isSelected = useMemo(() => selected.includes(identifier), [selected, data]);
    const isDisabled = disableSelectOptions?.disabledItems.includes(data[disableSelectOptions.key]);

    const toggleSelect = useCallback((evt) => {
        if (evt.target.checked) {
            onSelected(selected.concat(identifier));
        } else {
            onSelected(selected.filter((item) => item !== identifier));
        }
    }, [onSelected, selected]);

    return (
        <td className={classes.multiSelectStyle}>
            <input
                className={classes.inputStyle}
                type="checkbox"
                title={isDisabled ? disableSelectOptions?.title : 'Select row.'}
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
        id: PropTypes.string,
        jobId: PropTypes.string
    }),
    disableSelectOptions: PropTypes.shape({
        key: PropTypes.string,
        disabledItems: PropTypes.arrayOf(PropTypes.string),
        title: PropTypes.string
    }),
    title: PropTypes.string
};

export default MultiSelectCell;
