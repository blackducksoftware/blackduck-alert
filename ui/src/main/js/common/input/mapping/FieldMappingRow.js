import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { LabelFieldPropertyDefaults } from '../field/LabeledField';

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
            <button
                id="delete-mapping"
                className="btn btn-sm btn-primary p-2"
                type="button"
                onClick={() => deleteRow(index)}
            >
                <FontAwesomeIcon icon="minus" className="alert-icon" size="lg" />
            </button>
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
