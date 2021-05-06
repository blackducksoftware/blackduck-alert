import * as ConfigRequestBuilder from 'util/configurationRequestBuilder';

export const getDataById = async (id, csrfToken, errorHandler, setError) => {
    if (id) {
        const response = await ConfigRequestBuilder.createReadRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, id);
        const retrievedModel = await response.json();
        setError(errorHandler.handle(response, retrievedModel, true));
        return retrievedModel;
    }

    return null;
};
