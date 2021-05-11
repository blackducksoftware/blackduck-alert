import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';

export const getDataById = async (id, csrfToken) => {
    if (id) {
        const response = await ConfigRequestBuilder.createReadRequest(ConfigRequestBuilder.CONFIG_API_URL, csrfToken, id);
        const retrievedModel = await response.json();
        return retrievedModel;
    }

    return null;
};

export const getDataFindFirst = async (descriptorName, csrfToken) => {
    const response = await ConfigRequestBuilder.createReadAllGlobalContextRequest(csrfToken, descriptorName);
    const data = await response.json();

    const { fieldModels } = data;
    return (fieldModels && fieldModels.length > 0) ? fieldModels[0] : null;
};
