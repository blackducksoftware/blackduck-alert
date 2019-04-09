import React from 'react';
import SelectInput from 'field/input/SelectInput';
import TextInput from 'field/input/TextInput';
import TextArea from 'field/input/TextArea';
import PasswordInput from 'field/input/PasswordInput';
import NumberInput from 'field/input/NumberInput';
import CheckboxInput from 'field/input/CheckboxInput';
import ReadOnlyField from 'field/ReadOnlyField';
import * as FieldModelUtilities from 'util/fieldModelUtilities';

export function buildTextInput(props) {
    return <TextInput {...props} />;
}

export function buildTextArea(props) {
    return <TextArea {...props} />;
}

export function buildSelectInput(props) {
    return <SelectInput {...props} />;
}

export function buildPasswordInput(props) {
    return <PasswordInput {...props} />;
}

export function buildNumberInput(props) {
    return <NumberInput {...props} />;
}

export function buildCheckboxInput(props) {
    return <CheckboxInput {...props} />;
}

export function buildReadOnlyField(props) {
    return <ReadOnlyField {...props} />;
}

export const FIELDS = {
    TextInput: buildTextInput,
    TextArea: buildTextArea,
    Select: buildSelectInput,
    PasswordInput: buildPasswordInput,
    NumberInput: buildNumberInput,
    CheckboxInput: buildCheckboxInput,
    ReadOnlyField: buildReadOnlyField
};

export function getField(fieldType, props) {
    const field = FIELDS[fieldType];
    return field(props);
}

export function retrieveKeys(descriptorFields) {
    const fieldKeys = [];
    Object.keys(descriptorFields).forEach((key) => {
        const fieldKey = descriptorFields[key].key;
        fieldKeys.push(fieldKey);
    });

    return fieldKeys;
}

export function createField(field, value, isSet, fieldError, onChange) {
    const {
        key, label, description, type
    } = field;
    const checkedValue = value.toString().toLowerCase() === 'true';
    const propMapping = {
        id: key,
        name: key,
        description,
        label,
        value,
        isChecked: checkedValue,
        isSet,
        onChange,
        errorName: FieldModelUtilities.createFieldModelErrorKey(key),
        errorValue: fieldError
    };

    return getField(type, propMapping);
}
