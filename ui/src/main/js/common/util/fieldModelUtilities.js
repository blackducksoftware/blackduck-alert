export function getFieldModelSingleValue(fieldModel, key) {
    if (fieldModel && fieldModel.keyToValues) {
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
    if (fieldModel && fieldModel.keyToValues) {
        const fieldObject = fieldModel.keyToValues[key];
        if (fieldObject && fieldObject.values) {
            if (Object.keys(fieldObject.values).length > 0) {
                return fieldModel.keyToValues[key].values;
            }
        }
    }
    return [];
}

export function getFieldModelNumberValue(fieldModel, key) {
    const fieldValue = getFieldModelSingleValue(fieldModel, key);
    return parseInt(fieldValue, 10);
}

export function getFieldModelBooleanValue(fieldModel, key) {
    const fieldValue = getFieldModelSingleValue(fieldModel, key);
    if (fieldValue && fieldValue.toString()
        .toLowerCase() === 'true') {
        return true;
    }
    return false;
}

export function getFieldModelId(fieldModel) {
    return fieldModel.id || getFieldModelSingleValue(fieldModel, 'id');
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

export function hasValuesOrIsSet(fieldObject) {
    if (fieldObject) {
        const { isSet, values } = fieldObject;
        const valuesNotEmpty = values ? values.length > 0 : false;
        const everyValueIsNotEmpty = values ? values.every((item) => item !== '') : false;
        return isSet || (values && valuesNotEmpty && everyValueIsNotEmpty);
    }
    return false;
}

export function checkboxHasValue(fieldValue) {
    if (fieldValue) {
        const { values } = fieldValue;
        const valuesNotEmpty = values ? values.length > 0 : false;
        const everyValueIsNotEmpty = values ? values.every((item) => item === 'true') : false;
        return values && valuesNotEmpty && everyValueIsNotEmpty;
    }
    return false;
}

export function hasAnyValuesExcludingId(fieldModel) {
    const { keyToValues } = fieldModel;
    if (keyToValues) {
        return Object.keys(keyToValues)
            .find((key) => {
                if (key !== 'id') {
                    const fieldObject = keyToValues[key];
                    return hasValuesOrIsSet(fieldObject);
                }
                return false;
            });
    }
    return false;
}

function isCheckboxValue(values) {
    if (values && values.length > 0) {
        const value = values[0];
        return value === 'true' || value === 'false';
    }
    return false;
}

export function hasValue(fieldModel, key) {
    const { keyToValues } = fieldModel;
    if (keyToValues && keyToValues[key]) {
        const { values, isSet } = keyToValues[key];
        const hasCheckboxValue = checkboxHasValue(keyToValues[key]);
        return hasCheckboxValue || (isSet && !isCheckboxValue(values)) || (values && !isCheckboxValue(values));
    }

    return false;
}

export function updateFieldModelSingleValue(fieldModel, key, value) {
    // This is required to be sure we get the proper values from fieldModel
    const copy = JSON.parse(JSON.stringify(fieldModel));
    if (!copy.keyToValues) {
        copy.keyToValues = {};
    }

    if (!copy.keyToValues[key]) {
        copy.keyToValues[key] = {
            values: [],
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
    const copy = JSON.parse(JSON.stringify(fieldModel));
    if (!copy.keyToValues) {
        copy.keyToValues = {};
    }

    if (!copy.keyToValues[key]) {
        copy.keyToValues[key] = {
            values: [],
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
    const copy = {
        context: null,
        descriptorName: null,
        keyToValues: {}
    };

    if (!copy.context) {
        copy.context = sourceModel.context ? sourceModel.context : modelToAdd.context;
    }

    if (!copy.descriptorName) {
        copy.descriptorName = sourceModel.descriptorName ? sourceModel.descriptorName : modelToAdd.descriptorName;
    }

    const copyKeyToValues = (model) => {
        const sourceKeys = Object.keys(model.keyToValues);
        sourceKeys.forEach((key) => {
            copy.keyToValues[key] = model.keyToValues[key];
        });
    };

    if (sourceModel.keyToValues) {
        copyKeyToValues(sourceModel);
    }

    if (modelToAdd.keyToValues) {
        copyKeyToValues(modelToAdd);
    }

    return copy;
}

export function createEmptyFieldModel(fieldKeys, context, descriptorName) {
    const emptySettings = {};
    emptySettings.context = context;
    emptySettings.descriptorName = descriptorName;
    emptySettings.keyToValues = {};
    Object.keys(fieldKeys).forEach((key) => {
        emptySettings.keyToValues[fieldKeys[key]] = {
            values: [],
            isSet: false
        };
    });
    return emptySettings;
}

export function createFieldModelErrorKey(fieldKey) {
    return fieldKey.concat('Error');
}

export function createFieldModelWithDefaults(fields, context, descriptorName) {
    const emptySettings = {};
    emptySettings.context = context;
    emptySettings.descriptorName = descriptorName;
    emptySettings.keyToValues = {};
    Object.keys(fields).forEach((key) => {
        const specificField = fields[key];
        const { defaultValues } = specificField;
        const withDefault = (Array.isArray(defaultValues) && defaultValues.length > 0) ? defaultValues : [];
        emptySettings.keyToValues[specificField.key] = {
            values: withDefault,
            isSet: withDefault.length > 0
        };
    });
    return emptySettings;
}

export function hasKey(fieldModel, key) {
    const { keyToValues } = fieldModel;
    if (keyToValues) {
        return Object.keys(keyToValues).includes(key);
    }
    return false;
}

export function createFieldModelFromRequestedFields(fieldModel, requestedFields) {
    const newModel = {
        id: null,
        context: null,
        descriptorName: null,
        keyToValues: {}
    };

    if (fieldModel) {
        if (fieldModel.id) {
            newModel.id = fieldModel.id;
        }

        if (fieldModel.context) {
            newModel.context = fieldModel.context;
        }

        if (fieldModel.descriptorName) {
            newModel.descriptorName = fieldModel.descriptorName;
        }

        Object.keys(fieldModel.keyToValues)
            .filter((key) => requestedFields.includes(key))
            .forEach((key) => {
                const specificField = fieldModel.keyToValues[key];
                const fieldValues = (Array.isArray(specificField.values) && specificField.values.length > 0) ? specificField.values : [];
                newModel.keyToValues[key] = {
                    values: fieldValues,
                    isSet: specificField.isSet
                };
            });
    }
    return newModel;
}

export const handleChange = (data, setData) => ({ target }) => {
    const { type, name, value } = target;
    const updatedValue = type === 'checkbox' ? target.checked.toString() : value;
    const newState = Array.isArray(updatedValue) ? updateFieldModelValues(data, name, updatedValue) : updateFieldModelSingleValue(data, name, updatedValue);
    setData(newState);
};

// TODO: This name can be misleading, consider changing to createDefaultInputChangeHandler to separate from old FieldModels
export const handleTestChange = (testData, setTestData) => ({ target }) => {
    const { type, name, value } = target;
    const updatedValue = type === 'checkbox' ? target.checked.toString() : value;
    const newState = { ...testData, [name]: updatedValue };
    setTestData(newState);
};
