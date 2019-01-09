export function getFieldModelSingleValue(containerObject, key) {
    return containerObject.settingsData.keyToValues[key].values[0];
}

export function isFieldModelValueSet(containerObject, key) {
    return containerObject.settingsData.keyToValues[key].set;
}

export function createEmptyFieldModel(fields) {
    const emptySettings = {};
    emptySettings.context = 'GLOBAL';
    emptySettings.descriptorName = 'component_settings';
    emptySettings.keyToValues = {};
    Object.keys(fields).forEach((key) => {
        emptySettings.keyToValues[fields[key]] = {
            values: [''],
            isSet: false
        };
    });
    return emptySettings;
}

export function createFieldModelErrorKey(fieldKey) {
    return fieldKey.concat('Error');
}

export function checkModelOrCreateEmpty(fieldModel, fields) {
    if (Object.keys(fieldModel.keyToValues).length > 0) {
        return fieldModel;
    }
    return createEmptyFieldModel(fields);
}
