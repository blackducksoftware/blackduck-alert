import React from 'react';
import SelectInput from 'field/input/DynamicSelect';
import TextInput from 'field/input/TextInput';
import TextArea from 'field/input/TextArea';
import PasswordInput from 'field/input/PasswordInput';
import NumberInput from 'field/input/NumberInput';
import CheckboxInput from 'field/input/CheckboxInput';
import ReadOnlyField from 'field/ReadOnlyField';
import * as FieldModelUtilities from 'util/fieldModelUtilities';

export function buildTextInput(items) {
    return <TextInput {...items} />;
}

export function buildTextArea(items) {
    return <TextArea {...items} />;
}

export function buildSelectInput(items, field) {
    const { value } = items;
    const { searchable, multiSelect, options } = field;

    const selectValue = options.filter(option => option.value === value[0]);
    Object.assign(items, {
        value: selectValue, searchable, multiSelect, options
    });
    return <SelectInput {...items} />;
}

export function buildPasswordInput(items) {
    return <PasswordInput {...items} />;
}

export function buildNumberInput(items) {
    return <NumberInput {...items} />;
}

export function buildCheckboxInput(items) {
    const { value } = items;
    const checkedValue = value.toString().toLowerCase() === 'true';
    Object.assign(items, { isChecked: checkedValue });
    return <CheckboxInput {...items} />;
}

export function buildReadOnlyField(items) {
    return <ReadOnlyField {...items} />;
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

export function getField(fieldType, props, field) {
    const fieldFunction = FIELDS[fieldType];
    return fieldFunction(props, field);
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
    const propMapping = {
        id: key,
        name: key,
        description,
        label,
        value,
        isSet,
        onChange,
        errorName: FieldModelUtilities.createFieldModelErrorKey(key),
        errorValue: fieldError
    };

    return getField(type, propMapping, field);
}
