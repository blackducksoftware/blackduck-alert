import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/input/field/LabeledField';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import FieldMappingRow from 'common/input/mapping/FieldMappingRow';

const FluidFieldMappingField = ({
    id,
    description,
    value,
    setValue,
    errorName,
    errorValue,
    label,
    labelClass,
    readonly,
    required,
    showDescriptionPlaceHolder
}) => {
    const [fieldMappings, setFieldMappings] = useState([]);

    useEffect(() => {
        const valueList = Object.keys(value).map((key) => ({ [key]: value[key] }));
        valueList.push({});
        setFieldMappings(valueList);
    }, []);

    useEffect(() => {
        setValue(Object.fromEntries(fieldMappings));
    }, [fieldMappings]);

    const deleteMappingRow = (index) => {
        const removedPiece = fieldMappings.splice(index, 1);
        setFieldMappings([...fieldMappings]);
    };

    const updateMapping = (index, leftSide, rightSide) => {
        fieldMappings[index] = { [leftSide]: rightSide };
        setFieldMappings([...fieldMappings]);
    };

    const getKey = (mapping) => Object.keys(mapping)[0];

    const addRow = () => {
        fieldMappings.push({});
        setFieldMappings([...fieldMappings]);
    };

    const renderExistingRows = fieldMappings.map((mapping, index) => {
        const key = getKey(mapping);
        return (
            <FieldMappingRow
                index={index}
                setMapping={updateMapping}
                rightSide={mapping[key]}
                leftSide={key}
                deleteRow={deleteMappingRow}
            />
        );
    });

    return (
        <LabeledField
            id={id}
            description={description}
            errorName={errorName}
            errorValue={errorValue}
            label={label}
            labelClass={labelClass}
            required={required}
            showDescriptionPlaceHolder={showDescriptionPlaceHolder}
        >
            <>
                {renderExistingRows}
                <button
                    id={id}
                    className="btn btn-sm btn-primary"
                    type="button"
                    onClick={addRow}
                >
                    <FontAwesomeIcon icon="plus" className="alert-icon" size="lg" />
                </button>
            </>
        </LabeledField>
    );
};

FluidFieldMappingField.propTypes = {
    id: PropTypes.string,
    value: PropTypes.object.isRequired,
    setValue: PropTypes.func.isRequired,
    label: PropTypes.string.isRequired,
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    labelClass: PropTypes.string,
    readonly: PropTypes.bool,
    required: PropTypes.bool,
    showDescriptionPlaceHolder: PropTypes.bool
};

FluidFieldMappingField.defaultProps = {
    id: 'fieldMappingFieldId',
    readonly: false,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    labelClass: LabelFieldPropertyDefaults.LABEL_CLASS_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT,
    showDescriptionPlaceHolder: LabelFieldPropertyDefaults.SHOW_DESCRIPTION_PLACEHOLDER_DEFAULT
};

export default FluidFieldMappingField;
