export function getFieldModelSingleValue(fieldModel, key) {
    const fieldObject = fieldModel.keyToValues[key];
    if (fieldObject && fieldObject.values) {
        if (Object.keys(fieldObject.values).length > 0) {
            return fieldModel.keyToValues[key].values[0];
        }
    }
    return null;
}

export function isFieldModelValueSet(fieldModel, key) {
    const fieldObject = fieldModel.keyToValues[key];
    if (fieldObject) {
        return fieldObject.isSet;
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


export function createEmptyFieldModel(fields) {
    const emptySettings = {};
    emptySettings.context = 'GLOBAL';
    emptySettings.descriptorName = 'component_settings';
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
    const emptyFieldModel = createEmptyFieldModel(fields);
    const newModel = Object.assign({}, emptyFieldModel, fieldModel);
    newModel.keyToValues = Object.assign({}, emptyFieldModel.keyToValues, fieldModel.keyToValues);
    return newModel;
}
