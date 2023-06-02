import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import FieldMappingRow from 'common/component/input/mapping/FieldMappingRow';
import Button from 'common/component/button/Button';

const FluidFieldMappingField = ({
    id,
    buttonLabel,
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
        setFieldMappings(Object.keys(value).map((key) => ({ [key]: value[key] })));
    }, [value]);

    useEffect(() => {
        const updatedMappings = {};
        fieldMappings.filter((mapping) => Object.keys(mapping).length === 1).forEach((mapping) => { Object.assign(updatedMappings, mapping); });
        if (JSON.stringify(updatedMappings) !== JSON.stringify(value)) {
            setValue(updatedMappings);
        }
    }, [fieldMappings]);

    const deleteMappingRow = (index) => {
        fieldMappings.splice(index, 1);
        setFieldMappings([...fieldMappings]);
    };

    const updateMapping = (index, leftSide, rightSide) => {
        fieldMappings[index] = { [leftSide]: rightSide };
        setFieldMappings([...fieldMappings]);
    };

    const addRow = () => {
        fieldMappings.push({});
        setFieldMappings([...fieldMappings]);
    };

    const renderExistingRows = fieldMappings.map((mapping, index) => {
        const key = Object.keys(mapping)[0];
        return (
            <FieldMappingRow
                index={index}
                setMapping={updateMapping}
                rightSide={mapping[key]}
                leftSide={key}
                deleteRow={deleteMappingRow}
                readonly={readonly}
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
            <div className="d-inline-flex">
                <div className="container">
                    {renderExistingRows}
                    <Button id={id} onClick={addRow} text={buttonLabel} icon="plus" />
                </div>
            </div>
        </LabeledField>
    );
};

FluidFieldMappingField.propTypes = {
    id: PropTypes.string,
    value: PropTypes.object.isRequired,
    setValue: PropTypes.func.isRequired,
    label: PropTypes.string.isRequired,
    buttonLabel: PropTypes.string,
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
