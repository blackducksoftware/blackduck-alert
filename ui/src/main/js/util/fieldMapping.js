import React from 'react';
import SelectInput from 'field/input/DynamicSelectInput';
import TextInput from 'field/input/TextInput';
import TextArea from 'field/input/TextArea';
import PasswordInput from 'field/input/PasswordInput';
import NumberInput from 'field/input/NumberInput';
import CheckboxInput from 'field/input/CheckboxInput';
import ReadOnlyField from 'field/ReadOnlyField';
import * as FieldModelUtilities from 'util/fieldModelUtilities';
import CounterField from 'field/CounterField';
import TableSelectInput from 'field/input/TableSelectInput';
import EndpointButtonField from 'field/EndpointButtonField';
import EndpointSelectField from 'field/EndpointSelectField';
import UploadFileButtonField from 'field/UploadFileButtonField';
import OAuthEndpointButtonField from 'field/OAuthEndpointButtonField';
import FieldMappingField from 'field/FieldMappingField';

function extractFirstValue(items) {
    const { value } = items;
    return (Array.isArray(value)) ? value[0] : value;
}

function overwriteSingleValue(items) {
    const trimmedValue = extractFirstValue(items);
    return Object.assign(items, { value: trimmedValue });
}

function convertStringToBoolean(value) {
    return value.toString()
        .toLowerCase() === 'true';
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

function buildSelectInput(items, field) {
    const {
        searchable, multiSelect, options, readOnly, removeSelected, clearable
    } = field;

    const isReadOnly = convertStringToBoolean(readOnly);
    const isClearable = convertStringToBoolean(clearable);
    const isRemoveSelected = convertStringToBoolean(removeSelected);

    Object.assign(items, {
        searchable,
        multiSelect,
        readOnly: isReadOnly,
        removeSelected: isRemoveSelected,
        clearable: isClearable,
        options
    });
    return <SelectInput {...items} />;
}

function buildEndpointSelectInput(items, field, csrfToken) {
    const {
        searchable, multiSelect, readOnly, url, key, removeSelected, clearable, requiredRelatedFields
    } = field;

    const isReadOnly = convertStringToBoolean(readOnly);
    const isClearable = convertStringToBoolean(clearable);
    const isRemoveSelected = convertStringToBoolean(removeSelected);

    Object.assign(items, {
        searchable,
        multiSelect,
        readOnly: isReadOnly,
        removeSelected: isRemoveSelected,
        clearable: isClearable
    });
    return (
        <EndpointSelectField
            requiredRelatedFields={requiredRelatedFields}
            endpoint={url}
            fieldKey={key}
            csrfToken={csrfToken}
            {...items}
        />
    );
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

function buildHideCheckboxInput(items, field) {
    const { value } = items;
    const checkedValue = convertStringToBoolean(value);
    const { readOnly } = field;
    const isReadOnly = convertStringToBoolean(readOnly);
    Object.assign(items, {
        isChecked: checkedValue,
        readOnly: isReadOnly
    });
    return <CheckboxInput {...items} />;
}

function buildCheckboxInput(items, field) {
    const { value } = items;
    const checkedValue = convertStringToBoolean(value);
    const { readOnly } = field;
    const isReadOnly = convertStringToBoolean(readOnly);
    Object.assign(items, {
        isChecked: checkedValue,
        readOnly: isReadOnly
    });
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
    Object.assign(items, {
        countdown,
        value: trimmedValue
    });
    return <CounterField {...items} />;
}

function buildEndpointField(items, field, csrfToken) {
    const { value } = items;
    const {
        buttonLabel, url, successBox, subFields, key, requiredRelatedFields, readOnly
    } = field;
    const checkedValue = convertStringToBoolean(value);
    const isReadOnly = convertStringToBoolean(readOnly);
    Object.assign(items, {
        value: checkedValue,
        className: 'form-control',
        readOnly: isReadOnly
    });
    return (
        <EndpointButtonField
            fields={subFields}
            requiredRelatedFields={requiredRelatedFields}
            buttonLabel={buttonLabel}
            endpoint={url}
            successBox={successBox}
            fieldKey={key}
            csrfToken={csrfToken}
            {...items}
        />
    );
}

function buildTableSelectInput(items, field, csrfToken) {
    const {
        url, key, columns, paged, searchable, useRowAsValue, requiredRelatedFields
    } = field;
    const { readOnly } = field;
    const isReadOnly = convertStringToBoolean(readOnly);
    Object.assign(items, {
        readOnly: isReadOnly,
        paged,
        searchable,
        useRowAsValue
    });

    return <TableSelectInput endpoint={url} fieldKey={key} columns={columns} requiredRelatedFields={requiredRelatedFields} csrfToken={csrfToken} {...items} />;
}

function buildUploadFileButtonField(items, field, csrfToken) {
    const {
        buttonLabel, url, successBox, subFields, key, readOnly, accept, multiple
    } = field;
    const isReadOnly = convertStringToBoolean(readOnly);
    Object.assign(items, {
        className: 'form-control',
        readOnly: isReadOnly
    });
    return (
        <UploadFileButtonField
            fields={subFields}
            buttonLabel={buttonLabel}
            endpoint={url}
            successBox={successBox}
            fieldKey={key}
            accept={accept}
            multiple={multiple}
            csrfToken={csrfToken}
            {...items}
        />
    );
}

function buildOAuthEndpointField(items, field, csrfToken) {
    const { value } = items;
    const {
        buttonLabel, url, successBox, subFields, key, requiredRelatedFields, readOnly
    } = field;
    const checkedValue = convertStringToBoolean(value);
    const isReadOnly = convertStringToBoolean(readOnly);
    Object.assign(items, {
        value: checkedValue,
        className: 'form-control',
        readOnly: isReadOnly
    });
    return (
        <OAuthEndpointButtonField
            fields={subFields}
            requiredRelatedFields={requiredRelatedFields}
            buttonLabel={buttonLabel}
            endpoint={url}
            successBox={successBox}
            fieldKey={key}
            csrfToken={csrfToken}
            {...items}
        />
    );
}

function buildFieldMappingField(items, field, csrfToken) {
    const { value } = items;
    const {
        leftSide, rightSide, mappingTitle, key
    } = field;
    return (
        <FieldMappingField
            storedMappings={value}
            leftSideMapping={leftSide}
            rightSideMapping={rightSide}
            newMappingTitle={mappingTitle}
            fieldMappingKey={key}
            {...items}
        />
    );
}

export const FIELDS = {
    TextInput: buildTextInput,
    TextArea: buildTextArea,
    Select: buildSelectInput,
    EndpointSelectField: buildEndpointSelectInput,
    PasswordInput: buildPasswordInput,
    NumberInput: buildNumberInput,
    HideCheckboxInput: buildHideCheckboxInput,
    CheckboxInput: buildCheckboxInput,
    ReadOnlyField: buildReadOnlyField,
    CountdownField: buildCounterField,
    TableSelectInput: buildTableSelectInput,
    EndpointButtonField: buildEndpointField,
    UploadFileButtonField: buildUploadFileButtonField,
    OAuthEndpointButtonField: buildOAuthEndpointField,
    FieldMappingField: buildFieldMappingField
};

export function getField(fieldType, props, field, csrfToken) {
    const fieldFunction = FIELDS[fieldType];
    return fieldFunction(props, field, csrfToken);
}

export function retrieveKeys(descriptorFields) {
    const fieldKeys = [];
    Object.keys(descriptorFields)
        .forEach((key) => {
            const fieldKey = descriptorFields[key].key;
            fieldKeys.push(fieldKey);
        });

    return fieldKeys;
}

export function createField(field, currentConfig, fieldError, onChange, csrfToken) {
    const {
        key, label, description, type, required
    } = field;

    const value = FieldModelUtilities.getFieldModelValues(currentConfig, key);
    const isSet = FieldModelUtilities.isFieldModelValueSet(currentConfig, key);

    const propMapping = {
        key,
        id: key,
        name: key,
        description,
        label,
        value,
        isSet,
        onChange,
        required,
        errorName: FieldModelUtilities.createFieldModelErrorKey(key),
        errorValue: fieldError,
        currentConfig
    };

    return getField(type, propMapping, field, csrfToken);
}
