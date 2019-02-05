export function getFieldModelSingleValue(fieldModel, key) {
    const fieldObject = fieldModel.keyToValues[key];
    if (fieldObject && fieldObject.values) {
        if (Object.keys(fieldObject.values).length > 0) {
            return fieldModel.keyToValues[key].values[0];
        }
    }
    return undefined;
}

export function getFieldModelValues(fieldModel, key) {
    const fieldObject = fieldModel.keyToValues[key];
    if (fieldObject && fieldObject.values) {
        if (Object.keys(fieldObject.values).length > 0) {
            return fieldModel.keyToValues[key].values;
        }
    }
    return [];
}

export function getFieldModelBooleanValue(fieldModel, key) {
    const fieldValue = getFieldModelSingleValue(fieldModel, key);
    if (fieldValue) {
        const result = fieldValue === 'true';
        return result;
    }
    return false;
}

export function isFieldModelValueSet(fieldModel, key) {
    const fieldObject = fieldModel.keyToValues[key];
    if (fieldObject) {
        return fieldObject.isSet;
    }
    return false;
}

export function hasFieldModelValues(fieldModel, key) {
    const fieldObject = fieldModel.keyToValues[key];
    if (fieldObject) {
        return fieldObject.values && fieldObject.values.every(item => item !== '');
    }
    return false;
}

export function updateFieldModelSingleValue(fieldModel, key, value) {
    const copy = Object.assign({}, fieldModel);
    if (!copy.keyToValues[key]) {
        copy.keyToValues[key] = {
            values: [''],
            isSet: false
        };
    } else if (!copy.keyToValues[key].values) {
        copy.keyToValues[key].values = [];
        copy.keyToValues[key].isSet = false;
    }
    copy.keyToValues[key].values[0] = value;
    copy.keyToValues[key].isSet = false;
    return Object.assign({}, copy);
}

export function updateFieldModelValues(fieldModel, key, values) {
    const copy = Object.assign({}, fieldModel);
    if (!copy.keyToValues[key]) {
        copy.keyToValues[key] = {
            values: [''],
            isSet: false
        };
    } else if (!copy.keyToValues[key].values) {
        copy.keyToValues[key].values = [];
        copy.keyToValues[key].isSet = false;
    }
    copy.keyToValues[key].values = values;
    copy.keyToValues[key].isSet = false;
    return Object.assign({}, copy);
}


export function createEmptyFieldModel(fields, context, descriptorName) {
    const emptySettings = {};
    emptySettings.context = context;
    emptySettings.descriptorName = descriptorName;
    emptySettings.keyToValues = {};
    Object.keys(fields).forEach((key) => {
        emptySettings.keyToValues[fields[key]] = {
            values: null,
            isSet: false
        };
    });
    return emptySettings;
}

export function createFieldModelErrorKey(fieldKey) {
    return fieldKey.concat('Error');
}

export function checkModelOrCreateEmpty(fieldModel, fields) {
    const emptyFieldModel = createEmptyFieldModel(fields, fieldModel.context, fieldModel.descriptorName);
    const newModel = Object.assign({}, emptyFieldModel, fieldModel);
    const newKeyToValues = emptyFieldModel.keyToValues;
    if (fieldModel.keyToValues) {
        Object.keys(fieldModel.keyToValues).forEach((key) => {
            newKeyToValues[key] = fieldModel.keyToValues[key];
        });
    }
    newModel.keyToValues = newKeyToValues;
    return newModel;
}
