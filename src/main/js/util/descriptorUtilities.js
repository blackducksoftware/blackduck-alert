export const DESCRIPTOR_TYPE = {
    PROVIDER: 'PROVIDER',
    CHANNEL: 'CHANNEL',
    COMPONENT: 'COMPONENT'
}

export const CONTEXT_TYPE = {
    GLOBAL: 'GLOBAL',
    DISTRIBUTION: 'DISTRIBUTION'
}


export const DESCRIPTOR_NAME = {
    CHANNEL_EMAIL: 'channel_email',
    CHANNEL_HIPCHAT: 'channel_hipchat',
    CHANNEL_SLACK: 'channel_slack',
    COMPONENT_SCHEDULING: 'component_scheduling',
    COMPONENT_SETTINGS: 'component_settings',
    PROVIDER_BLACKDUCK: 'provider_blackduck'
}

export function findDescriptorByTypeAndContext(descriptorList, descriptorType, context) {
    if (!descriptorList) {
        return null;
    }
    const resultList = descriptorList.filter(descriptor => descriptor.type === descriptorType && descriptor.context === context);
    if (!resultList) {
        return null;
    }

    return resultList;
}

export function findDescriptorField(descriptor, fieldKey) {
    const fieldArray = descriptor.fields;
    if (fieldArray) {
        return fieldArray.find(field => field.key === fieldKey);
    }
    return null;
}

export function findDescriptorFieldOptions(descriptor, fieldKey) {
    const field = findDescriptorField(descriptor, fieldKey);
    if (field) {
        const { options } = field;
        return options;
    }
    return [];
}

