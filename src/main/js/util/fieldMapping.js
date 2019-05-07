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

function trimValue(items) {
    const { value } = items;
    const trimmedValue = (Array.isArray(value)) ? value[0] : value;

    Object.assign(items, { value: trimmedValue });
    return items;
}

function buildTextInput(items) {
    const trimmedValue = trimValue(items);
    return <TextInput {...trimmedValue} />;
}

function buildTextArea(items) {
    const trimmedValue = trimValue(items);
    return <TextArea {...trimmedValue} />;
}

const { Option, SingleValue } = components;

function buildSelectInput(items, field) {
    const { value } = items;
    const { searchable, multiSelect, options } = field;

    const selectValue = options.filter(option => value.includes(option.value));

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
        value: selectValue, searchable, multiSelect, options, components: { Option: typeOptionLabel, SingleValue: typeLabel }
    });
    return <SelectInput {...items} />;
}

function buildPasswordInput(items) {
    const trimmedValue = trimValue(items);
    return <PasswordInput {...trimmedValue} />;
}

function buildNumberInput(items) {
    const trimmedValue = trimValue(items);
    return <NumberInput {...trimmedValue} />;
}

function buildCheckboxInput(items) {
    const { value } = items;
    const checkedValue = value.toString().toLowerCase() === 'true';
    Object.assign(items, { isChecked: checkedValue });
    return <CheckboxInput {...items} />;
}

function buildReadOnlyField(items) {
    const trimmedValue = trimValue(items);
    return <ReadOnlyField {...trimmedValue} />;
}

function buildCounterField(items, field) {
    const { countdown } = field;
    const { value } = items;
    const trimmedValue = (value.length > 0) && value[0];
    Object.assign(items, { countdown, value: trimmedValue });
    return <CounterField {...items} />;
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
