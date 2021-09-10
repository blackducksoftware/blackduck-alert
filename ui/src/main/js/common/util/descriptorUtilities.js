export const DESCRIPTOR_TYPE = {
    PROVIDER: 'PROVIDER',
    CHANNEL: 'CHANNEL',
    COMPONENT: 'COMPONENT'
};

export const CONTEXT_TYPE = {
    GLOBAL: 'GLOBAL',
    DISTRIBUTION: 'DISTRIBUTION'
};

export const DESCRIPTOR_NAME = {
    CHANNEL_EMAIL: 'channel_email',
    CHANNEL_SLACK: 'channel_slack',
    COMPONENT_AUDIT: 'component_audit',
    COMPONENT_SCHEDULING: 'component_scheduling',
    COMPONENT_SETTINGS: 'component_settings',
    PROVIDER_BLACKDUCK: 'provider_blackduck',
    COMPONENT_USERS: 'component_users',
    COMPONENT_CERTIFICATES: 'component_certificates',
    COMPONENT_TASKS: 'component_tasks'
};

export const OPERATIONS = {
    CREATE: 'CREATE',
    DELETE: 'DELETE',
    READ: 'READ',
    WRITE: 'WRITE',
    EXECUTE: 'EXECUTE',
    UPLOAD_FILE_READ: 'UPLOAD_FILE_READ',
    UPLOAD_FILE_WRITE: 'UPLOAD_FILE_WRITE',
    UPLOAD_FILE_DELETE: 'UPLOAD_FILE_DELETE'
};

export function findDescriptorByNameAndContext(descriptorList, descriptorName, context) {
    if (!descriptorList) {
        return null;
    }
    const resultList = descriptorList.filter((descriptor) => descriptor.name === descriptorName && descriptor.context === context);
    if (!resultList) {
        return null;
    }

    return resultList;
}

export function findFirstDescriptorByNameAndContext(descriptorList, descriptorName, context) {
    if (!descriptorList) {
        return null;
    }
    return descriptorList.find((descriptor) => descriptor.name === descriptorName && descriptor.context === context);
}

export function findDescriptorByTypeAndContext(descriptorList, descriptorType, context) {
    if (!descriptorList) {
        return null;
    }
    const resultList = descriptorList.filter((descriptor) => descriptor.type === descriptorType && descriptor.context === context);
    if (!resultList) {
        return null;
    }

    return resultList;
}

export function findDescriptorByType(descriptorList, descriptorType) {
    if (!descriptorList) {
        return null;
    }
    const resultList = descriptorList.filter((descriptor) => descriptor.type === descriptorType);
    if (!resultList) {
        return null;
    }

    return resultList;
}

export function isOperationAssigned(descriptor, operationName) {
    if (descriptor) {
        return descriptor.operations.find((operation) => operation === operationName) !== undefined;
    }
    return false;
}

export function isOneOperationAssigned(descriptor, operationArray) {
    if (!operationArray) {
        return false;
    }
    return operationArray.find((operation) => isOperationAssigned(descriptor, operation)) !== undefined;
}

export function doesDescriptorExist(descriptorMap, key) {
    return Object.prototype.hasOwnProperty.call(descriptorMap, key);
}

// TODO hasTestFields probably isn't necessary now that everything is static
export function getButtonPermissions(descriptor, hasTestFields) {
    if (!descriptor) {
        return [false, false, false];
    }
    const { type } = descriptor;
    const includeTestButton = (type !== DESCRIPTOR_TYPE.COMPONENT) || hasTestFields;
    const displayTest = isOperationAssigned(descriptor, OPERATIONS.EXECUTE) && includeTestButton;
    const displaySave = isOneOperationAssigned(descriptor, [OPERATIONS.CREATE, OPERATIONS.WRITE]);
    const displayDelete = isOperationAssigned(descriptor, OPERATIONS.DELETE) && (type !== DESCRIPTOR_TYPE.COMPONENT);

    return [displayTest, displaySave, displayDelete];
}
