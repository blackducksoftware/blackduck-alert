import React from 'react';
import { components } from 'react-select';
import SelectInput from 'field/input/DynamicSelect';
import TextInput from 'field/input/TextInput';
import TextArea from 'field/input/TextArea';
import PasswordInput from 'field/input/PasswordInput';
import NumberInput from 'field/input/NumberInput';
import CheckboxInput from 'field/input/CheckboxInput';
import ReadOnlyField from 'field/ReadOnlyField';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import CounterField from 'field/CounterField';
import DescriptorOption from 'component/common/DescriptorOption';

function extractFirstValue(items) {
    const { value } = items;
    return (Array.isArray(value)) ? value[0] : value;
}

function overwriteSingleValue(items) {
    const trimmedValue = extractFirstValue(items);
    return Object.assign(items, { value: trimmedValue });
}

function buildTextInput(items, field) {
    const trimmedValue = overwriteSingleValue(items);
    const { readOnly } = field;
    const isReadOnly = convertStringToBoolean(readOnly);
    Object.assign(trimmedValue, { readOnly: isReadOnly });
    return <TextInput {...trimmedValue} />;
}

function buildTextArea(items, field) {
    const trimmedValue = overwriteSingleValue(items);
    const { readOnly } = field;
    const isReadOnly = convertStringToBoolean(readOnly);
    Object.assign(trimmedValue, { readOnly: isReadOnly });
    return <TextArea {...trimmedValue} />;
}

const { Option, SingleValue } = components;

function buildSelectInput(items, field) {
    const { value } = items;
    const { searchable, multiSelect, options, readOnly } = field;

    const selectValue = options.filter(option => value.includes(option.value));
    const isReadOnly = convertStringToBoolean(readOnly);
    const typeOptionLabel = props => (
        <Option {...props}>
            <DescriptorOption icon={props.data.icon} label={props.data.label} value={props.data.value} />
        </Option>
    );

    const typeLabel = props => (
        <SingleValue {...props}>
            <DescriptorOption icon={props.data.icon} label={props.data.label} value={props.data.value} />
        </SingleValue>
    );

    Object.assign(items, {
        value: selectValue, searchable, multiSelect, readOnly: isReadOnly, options, components: { Option: typeOptionLabel, SingleValue: typeLabel }
    });
    return <SelectInput {...items} />;
}

function buildPasswordInput(items, field) {
    const trimmedValue = overwriteSingleValue(items);
    const { readOnly } = field;
    const isReadOnly = convertStringToBoolean(readOnly);
    Object.assign(trimmedValue, { readOnly: isReadOnly });
    return <PasswordInput {...trimmedValue} />;
}

function buildNumberInput(items, field) {
    const trimmedValue = overwriteSingleValue(items);
    const { readOnly } = field;
    const isReadOnly = convertStringToBoolean(readOnly);
    Object.assign(trimmedValue, { readOnly: isReadOnly });
    return <NumberInput {...trimmedValue} />;
}

function buildCheckboxInput(items, field) {
    const { value } = items;
    const checkedValue = convertStringToBoolean(value);
    const { readOnly } = field;
    const isReadOnly = convertStringToBoolean(readOnly);
    Object.assign(items, { isChecked: checkedValue, readOnly: isReadOnly });
    return <CheckboxInput {...items} />;
}

function buildReadOnlyField(items, field) {
    const trimmedValue = overwriteSingleValue(items);
    const { readOnly } = field;
    Object.assign(trimmedValue, { readOnly });
    return <ReadOnlyField {...trimmedValue} />;
}

function buildCounterField(items, field) {
    const { countdown } = field;
    const trimmedValue = extractFirstValue(items);
    Object.assign(items, { countdown, value: trimmedValue });
    return <CounterField {...items} />;
}

function convertStringToBoolean(value) {
    return value.toString().toLowerCase() === 'true';
}

export const FIELDS = {
    TextInput: buildTextInput,
    TextArea: buildTextArea,
    Select: buildSelectInput,
    PasswordInput: buildPasswordInput,
    NumberInput: buildNumberInput,
    CheckboxInput: buildCheckboxInput,
    ReadOnlyField: buildReadOnlyField,
    CountdownField: buildCounterField
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
        key,
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
