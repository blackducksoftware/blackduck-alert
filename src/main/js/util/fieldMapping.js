import React from 'react';
import SelectInput from 'field/input/SelectInput';
import TextInput from 'field/input/TextInput';
import TextArea from 'field/input/TextArea';
import PasswordInput from 'field/input/PasswordInput';
import NumberInput from 'field/input/NumberInput';
import CheckboxInput from 'field/input/CheckboxInput';
import ReadOnlyField from 'field/ReadOnlyField';
import * as FieldModelUtilities from 'util/fieldModelUtilities';

export function getField(fieldType, props) {
    switch (fieldType) {
        case 'Select':
            return <SelectInput {...props} />;
        case 'TextInput':
            return <TextInput {...props} />;
        case 'TextArea':
            return <TextArea {...props} />;
        case 'PasswordInput':
            return <PasswordInput {...props} />;
        case 'NumberInput':
            return <NumberInput {...props} />;
        case 'CheckboxInput':
            return <CheckboxInput {...props} />;
        case 'ReadOnlyField':
            return <ReadOnlyField {...props} />;
        default:
            return <TextInput {...props} />;
    }
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

    return getField(type, propMapping);
}
