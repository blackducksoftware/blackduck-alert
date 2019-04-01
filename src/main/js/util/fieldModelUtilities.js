export function getFieldDescription(fieldDescriptions, key) {
    if (fieldDescriptions.hasOwnProperty(key)) {
        return fieldDescriptions[key];
    }
    return undefined;
}

export function getFieldModelSingleValue(fieldModel, key) {
    if (fieldModel.keyToValues) {
        const fieldObject = fieldModel.keyToValues[key];
        if (fieldObject && fieldObject.values) {
            if (Object.keys(fieldObject.values).length > 0) {
                return fieldModel.keyToValues[key].values[0];
            }
        }
    }
    return undefined;
}

export function getFieldModelSingleValueOrDefault(fieldModel, key, defaultValue) {
    return getFieldModelSingleValue(fieldModel, key) || defaultValue;
}

export function getFieldModelValues(fieldModel, key) {
    if (fieldModel.keyToValues) {
        const fieldObject = fieldModel.keyToValues[key];
        if (fieldObject && fieldObject.values) {
            if (Object.keys(fieldObject.values).length > 0) {
                return fieldModel.keyToValues[key].values;
            }
        }
    }
    return [];
}

export function getFieldModelBooleanValue(fieldModel, key) {
    const fieldValue = getFieldModelSingleValue(fieldModel, key);
    if (fieldValue && fieldValue.toString().toLowerCase() === 'true') {
        return true;
    }
    return false;
}

export function getFieldModelBooleanValueOrDefault(fieldModel, key, defaultValue) {
    return getFieldModelBooleanValue(fieldModel, key) || defaultValue;
}

export function isFieldModelValueSet(fieldModel, key) {
    if (fieldModel.keyToValues) {
        const fieldObject = fieldModel.keyToValues[key];
        if (fieldObject) {
            return fieldObject.isSet;
        }
    }
    return false;
}

export function hasFieldModelValues(fieldModel, key) {
    if (fieldModel.keyToValues) {
        const fieldObject = fieldModel.keyToValues[key];
        if (fieldObject) {
            return fieldObject.values && fieldObject.values.length > 0 && fieldObject.values.every(item => item !== '');
        }
    }
    return false;
}

export function keysHaveValueOrIsSet(fieldModel, keys) {
    let hasValue = false;
    if (fieldModel.keyToValues && keys) {
        const found = keys.find((key) => {
            const fieldObject = fieldModel.keyToValues[key];
            if (fieldObject) {
                const { isSet, values } = fieldObject;
                const valuesNotEmpty = values ? values.length > 0 : false;
                const everyValueIsNotEmpty = values ? values.every(item => item !== '') : false;
                return isSet || (values && valuesNotEmpty && everyValueIsNotEmpty);
            }
            return false;
        });
        if (found) {
            hasValue = true;
        }
    }
    return hasValue;
}

export function updateFieldModelSingleValue(fieldModel, key, value) {
    const copy = Object.assign({}, fieldModel);
    if (!copy.keyToValues) {
        copy.keyToValues = {};
    }

    if (!copy.keyToValues[key]) {
        copy.keyToValues[key] = {
            values: [''],
            isSet: false
        };
    } else if (!copy.keyToValues[key].values) {
        copy.keyToValues[key].values = [];
        copy.keyToValues[key].isSet = false;
    }

    if (value !== undefined || value !== null) {
        copy.keyToValues[key].values[0] = value;
    }
    copy.keyToValues[key].isSet = false;
    return copy;
}

export function updateFieldModelValues(fieldModel, key, values) {
    const copy = Object.assign({}, fieldModel);
    if (!copy.keyToValues) {
        copy.keyToValues = {};
    }

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
    return copy;
}

export function combineFieldModels(sourceModel, modelToAdd) {
    const copy = Object.assign({}, sourceModel);
    if (!copy.context) {
        copy.context = modelToAdd.context;
    }

    if (!copy.descriptorName) {
        copy.descriptorName = modelToAdd.descriptorName;
    }
    copy.keyToValues = Object.assign({}, sourceModel.keyToValues, modelToAdd.keyToValues);

    return copy;
}

export function createEmptyFieldModelFromFieldObject(fieldObjects, context, descriptorName) {
    const emptySettings = {};
    emptySettings.context = context;
    emptySettings.descriptorName = descriptorName;
    emptySettings.keyToValues = {};
    Object.keys(fieldObjects).forEach((key) => {
        emptySettings.keyToValues[fieldObjects[key].key] = {
            values: null,
            isSet: false
        };
    });
    return emptySettings;
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
