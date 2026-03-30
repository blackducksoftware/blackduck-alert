import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import BaseInput from 'common/component/input/BaseInput';
import Button from '../../button/Button';

const useStyles = createUseStyles((theme) => ({
    fieldMappingRow: {
        display: 'flex',
        flexDirection: 'column',
        gap: '8px'
    },
    fieldMappingContent: {
        display: 'flex',
        flexDirection: 'row',
        columnGap: '10px',
        alignItems: 'center'
    },
    deleteRowButton: {
        background: 'none',
        border: 'none',
        fontSize: '14px',
        borderRadius: '8px',
        padding: ['5px', '10px'],
        color: theme.colors.grey.lightGrey,
        '&:hover': {
            color: theme.colors.status.error.text,
            backgroundColor: theme.colors.status.error.background
        }
    }
}));

const FieldMappingRow = ({
    index,
    leftSide,
    rightSide,
    setMapping,
    deleteRow,
    mappingSymbol,
    readonly
}) => {
    const classes = useStyles();
    const [currentLeftSide, setCurrentLeftSide] = useState(leftSide);
    const [currentRightSide, setCurrentRightSide] = useState(rightSide);

    useEffect(() => {
        setMapping(index, currentLeftSide, currentRightSide);
    }, [currentLeftSide, currentRightSide]);

    useEffect(() => {
        setCurrentLeftSide(leftSide);
        setCurrentRightSide(rightSide);
    }, [leftSide, rightSide]);

    return (
        <div className={classes.fieldMappingRow}>
            <div className={classes.fieldMappingContent}>
                <div className="col-sm">
                    <BaseInput
                        id="left-side"
                        type="text"
                        readOnly={readonly}
                        name="left-side"
                        value={currentLeftSide}
                        onChange={({ target }) => setCurrentLeftSide(target.value)}
                        placeholder="Property Name"
                    />
                </div>
                {mappingSymbol}
                <div className="col-sm">
                    <BaseInput
                        id="right-side"
                        type="text"
                        readOnly={readonly}
                        name="right-side"
                        value={currentRightSide}
                        onChange={({ target }) => setCurrentRightSide(target.value)}
                        placeholder="Property Value"
                    />
                </div>
            </div>
            <Button id="delete-mapping" onClick={() => deleteRow(index)} icon="x" text="Remove Property" buttonStyle="actionSecondaryDelete" />
        </div>
    );
};

FieldMappingRow.propTypes = {
    index: PropTypes.number.isRequired,
    setMapping: PropTypes.func.isRequired,
    deleteRow: PropTypes.func.isRequired,
    leftSide: PropTypes.string,
    rightSide: PropTypes.string,
    mappingSymbol: PropTypes.any,
    readonly: PropTypes.bool
};

FieldMappingRow.defaultProps = {
    leftSide: '',
    rightSide: '',
    mappingSymbol: ' = ',
    readonly: false
};

export default FieldMappingRow;
