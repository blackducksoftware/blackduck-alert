export const DESCRIPTOR_TYPE = {
    PROVIDER: 'PROVIDER',
    CHANNEL: 'CHANNEL',
    COMPONENT: 'COMPONENT'
}

export const CONTEXT_TYPE = {
    GLOBAL: 'GLOBAL',
    DISTRIBUTION: 'DISTRIBUTION'
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
