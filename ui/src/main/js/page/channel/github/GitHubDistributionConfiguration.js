import PropTypes from 'prop-types';
import React from 'react';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import EndpointSelectField from 'common/component/input/EndpointSelectField';
import TextInput from 'common/component/input/TextInput';
import { createReadRequest } from 'common/util/configurationRequestBuilder';
import { DISTRIBUTION_COMMON_FIELD_KEYS } from 'page/distribution/DistributionModel';
import { GITHUB_DISTRIBUTION_FIELD_KEYS } from 'page/channel/github/GitHubModel';

const GitHubDistributionConfiguration = ({
    csrfToken, data, setData, errors, readonly
}) => {
    const readRequest = () => {
        const apiUrl = '/alert/api/configuration/github?pageNumber=0&pageSize=25';
        return createReadRequest(apiUrl, csrfToken);
    };

    const convertDataToOptions = (responseData) => {
        const { models } = responseData;
        console.log(models);
        return models.map((configurationModel) => {
            const { id: configId, name } = configurationModel;
            return {
                key: configId,
                label: name,
                value: configId
            };
        });
    };

    return (
        <>
            <EndpointSelectField
                id={DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId}
                csrfToken={csrfToken}
                endpoint="/api/configuration/github"
                fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId}
                label="Github User"
                description="Select a Github User that will be used to create or update issues. Please note the options are limited to the first 25 Github users."
                readOnly={readonly}
                required
                readOptionsRequest={readRequest}
                convertDataToOptions={convertDataToOptions}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelValues(data, DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId)}
                errorValue={errors.fieldErrors[DISTRIBUTION_COMMON_FIELD_KEYS.channelGlobalConfigId]}
            />
            <TextInput
                id={GITHUB_DISTRIBUTION_FIELD_KEYS.repositoryUrl}
                label="GitHub Repository URL"
                description="The URL of the GitHub repository assigned to receive Upgrade Guidance."
                name={GITHUB_DISTRIBUTION_FIELD_KEYS.repositoryUrl}
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValue(data, GITHUB_DISTRIBUTION_FIELD_KEYS.repositoryUrl)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(GITHUB_DISTRIBUTION_FIELD_KEYS.repositoryUrl)}
                errorValue={errors.fieldErrors[GITHUB_DISTRIBUTION_FIELD_KEYS.repositoryUrl]}
            />
        </>
    );
};

GitHubDistributionConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    data: PropTypes.object.isRequired,
    setData: PropTypes.func.isRequired,
    errors: PropTypes.object.isRequired,
    readonly: PropTypes.bool.isRequired
};

export default GitHubDistributionConfiguration;
