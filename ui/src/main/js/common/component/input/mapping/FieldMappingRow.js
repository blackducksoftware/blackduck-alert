import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import Button from 'common/component/button/Button';

const FieldMappingRow = ({
    index,
    leftSide,
    rightSide,
    setMapping,
    deleteRow,
    mappingSymbol,
    readonly
}) => {
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
        <div className="row align-items-start form-group">
            <div className="col-sm">
                <input
                    id="left-side"
                    type="text"
                    readOnly={readonly}
                    className="form-control"
                    name="left-side"
                    value={currentLeftSide}
                    onChange={({ target }) => setCurrentLeftSide(target.value)}
                />
            </div>
            {mappingSymbol}
            <div className="col-sm">
                <input
                    id="right-side"
                    type="text"
                    readOnly={readonly}
                    className="form-control"
                    name="right-side"
                    value={currentRightSide}
                    onChange={({ target }) => setCurrentRightSide(target.value)}
                />
            </div>
            <Button id="delete-mapping" onClick={() => deleteRow(index)} text="Remove Property" icon="minus" />
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
