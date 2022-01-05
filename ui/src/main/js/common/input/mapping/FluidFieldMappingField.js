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

    // We need this instead of useEffect with empty dependencies because we render initially without data
    const [startupFlag, setStartupFlag] = useState(true);

    useEffect(() => {
        if (startupFlag && Object.keys(value).length >= 1) {
            const valueList = Object.keys(value).map((key) => ({ [key]: value[key] }));
            setFieldMappings(valueList);
            setStartupFlag(false);
        }
    });

    useEffect(() => {
        const updatedMappings = {};
        fieldMappings.filter((mapping) => Object.keys(mapping).length === 1).forEach((mapping) => {
            Object.assign(updatedMappings, mapping);
        });
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
                    <button
                        id={id}
                        className="btn btn-sm btn-primary"
                        type="button"
                        onClick={addRow}
                    >
                        <FontAwesomeIcon icon="plus" className="alert-icon" size="lg" />
                    </button>
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
