import React, { useEffect, useState } from 'react';
import PropTypes from 'prop-types';
import LabeledField, { LabelFieldPropertyDefaults } from 'common/component/input/field/LabeledField';
import FieldMappingRow from 'common/component/input/mapping/FieldMappingRow';
import Button from 'common/component/button/Button';
import { createUseStyles } from 'react-jss';

const useStyles = createUseStyles((theme) => ({
    additionalFieldsContainer: {
        borderTop: `1px solid ${theme.colors.defaultBackgroundColor}`,
        padding: ['20px', 0],
        marginTop: '20px'
    },
    additionalFieldsContent: {
        width: '100%',
        display: 'flex',
        flexDirection: 'column',
        rowGap: '10px'
    },
    additionalFieldsDescription: {
        fontSize: '14px',
        color: theme.colors.grey.blackout
    }
}));

const FluidFieldMappingField = ({
    id,
    buttonLabel,
    description,
    value,
    setValue,
    errorName,
    errorValue,
    readonly,
    required
}) => {
    const classes = useStyles();
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
                key={key}
            />
        );
    });

    return (
        <div className={classes.additionalFieldsContainer}>
            <h5>Additional Properties</h5>
            <LabeledField
                id={id}
                errorName={errorName}
                errorValue={errorValue}
                label=""
                required={required}
            >
                <p className={classes.additionalFieldsDescription}>{description}</p>
                <div className={classes.additionalFieldsContent}>
                    {renderExistingRows}
                    <div>
                        <Button id={id} onClick={addRow} text={buttonLabel} icon="plus" buttonStyle="actionSecondary" />
                    </div>
                </div>
            </LabeledField>
        </div>
    );
};

FluidFieldMappingField.propTypes = {
    id: PropTypes.string,
    value: PropTypes.object.isRequired,
    setValue: PropTypes.func.isRequired,
    buttonLabel: PropTypes.string,
    description: PropTypes.string,
    errorName: PropTypes.string,
    errorValue: PropTypes.object,
    readonly: PropTypes.bool,
    required: PropTypes.bool
};

FluidFieldMappingField.defaultProps = {
    id: 'fieldMappingFieldId',
    readonly: false,
    description: LabelFieldPropertyDefaults.DESCRIPTION_DEFAULT,
    errorName: LabelFieldPropertyDefaults.ERROR_NAME_DEFAULT,
    errorValue: LabelFieldPropertyDefaults.ERROR_VALUE_DEFAULT,
    required: LabelFieldPropertyDefaults.REQUIRED_DEFAULT
};

export default FluidFieldMappingField;
