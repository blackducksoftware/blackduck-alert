import * as ConfigRequestBuilder from 'common/util/configurationRequestBuilder';
import HeaderUtilities from 'common/util/HeaderUtilities';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';

export const getDataById = async (id, csrfToken, errorHandler, setError) => {
    if (id) {
        const response = await ConfigRequestBuilder.createReadRequest(ConfigRequestBuilder.JOB_API_URL, csrfToken, id);
        const retrievedModel = await response.json();
        setError(errorHandler.handle(response, retrievedModel, true));
        return retrievedModel;
    }

    return null;
};

export const checkDescriptorForGlobalConfig = ({
    csrfToken, descriptorName, errorHandler, fieldName, errors, setErrors
}) => {
    const url = `${ConfigRequestBuilder.JOB_API_URL}/descriptorCheck`;
    const headersUtil = new HeaderUtilities();
    headersUtil.addApplicationJsonContentType();
    headersUtil.addXCsrfToken(csrfToken);
    const request = fetch(url, {
        credentials: 'same-origin',
        method: 'POST',
        body: descriptorName,
        headers: headersUtil.getHeaders()
    });
    request.then((response) => {
        if (response.ok) {
            const errorObject = HttpErrorUtilities.createEmptyErrorObject();
            errorObject.fieldErrors[fieldName] = {};
            const newErrorObject = HttpErrorUtilities.combineErrorObjects(errors, errorObject);
            setErrors(newErrorObject);
        } else {
            response.json().then((data) => {
                const errorObject = errorHandler.handle(response, data, false);
                const warning = HttpErrorUtilities.createFieldWarning(errorObject.message);
                errorObject.fieldErrors[fieldName] = warning;
                const newErrorObject = HttpErrorUtilities.combineErrorObjects(errors, errorObject);
                setErrors(newErrorObject);
            });
        }
    }).catch(console.error);
};
