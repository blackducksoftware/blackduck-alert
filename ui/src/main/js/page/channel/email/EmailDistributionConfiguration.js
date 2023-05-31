import React from 'react';
import PropTypes from 'prop-types';
import TextInput from 'common/component/input/TextInput';
import { EMAIL_DISTRIBUTION_ATTACHMENT_OPTIONS, EMAIL_DISTRIBUTION_FIELD_KEYS } from 'page/channel/email/EmailModels';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import CheckboxInput from 'common/component/input/CheckboxInput';
import DynamicSelectInput from 'common/component/input/DynamicSelectInput';
import { DISTRIBUTION_URLS } from 'page/distribution/DistributionModel';
import { createNewConfigurationRequest } from 'common/util/configurationRequestBuilder';
import EndpointSelectField from 'common/component/input/EndpointSelectField';

const EmailDistributionConfiguration = ({
    csrfToken, data, setData, errors, readonly, createAdditionalEmailRequestBody
}) => {
    const getEmailsRequest = () => {
        const apiUrl = '/alert/api/function/email.additional.addresses?pageNumber=0&pageSize=10&searchTerm=';
        return createNewConfigurationRequest(apiUrl, csrfToken, createAdditionalEmailRequestBody());
    };

    const convertDataToOptions = (responseData) => {
        const { models } = responseData;
        return models.map((emailModel) => {
            const { emailAddress } = emailModel;
            return {
                key: emailAddress,
                label: emailAddress,
                value: emailAddress
            };
        });
    };

    return (
        <>
            <TextInput
                id={EMAIL_DISTRIBUTION_FIELD_KEYS.subject}
                key={EMAIL_DISTRIBUTION_FIELD_KEYS.subject}
                name={EMAIL_DISTRIBUTION_FIELD_KEYS.subject}
                label="Subject Line"
                description="The subject line to use in the emails sent for this distribution job."
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelSingleValue(data, EMAIL_DISTRIBUTION_FIELD_KEYS.subject)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_DISTRIBUTION_FIELD_KEYS.subject)}
                errorValue={errors.fieldErrors[EMAIL_DISTRIBUTION_FIELD_KEYS.subject]}
            />
            <EndpointSelectField
                searchable
                id={EMAIL_DISTRIBUTION_FIELD_KEYS.additionalAddresses}
                csrfToken={csrfToken}
                endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                fieldKey={EMAIL_DISTRIBUTION_FIELD_KEYS.additionalAddresses}
                label="Additional Email Addresses"
                description="Any additional email addresses (for valid users of the provider) that notifications from this job should be sent to."
                multiSelect
                readOnly={readonly}
                readOptionsRequest={getEmailsRequest}
                convertDataToOptions={convertDataToOptions}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelValues(data, EMAIL_DISTRIBUTION_FIELD_KEYS.additionalAddresses)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_DISTRIBUTION_FIELD_KEYS.additionalAddresses)}
                errorValue={errors.fieldErrors[EMAIL_DISTRIBUTION_FIELD_KEYS.additionalAddresses]}
            />
            <CheckboxInput
                id={EMAIL_DISTRIBUTION_FIELD_KEYS.additionalAddressesOnly}
                name={EMAIL_DISTRIBUTION_FIELD_KEYS.additionalAddressesOnly}
                label="Additional Email Addresses Only"
                description="Rather than sending emails to users assigned to the configured projects, send emails to only the users selected in 'Additional Email Addresses'."
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                isChecked={FieldModelUtilities.getFieldModelBooleanValue(data, EMAIL_DISTRIBUTION_FIELD_KEYS.additionalAddressesOnly)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_DISTRIBUTION_FIELD_KEYS.additionalAddressesOnly)}
                errorValue={errors.fieldErrors[EMAIL_DISTRIBUTION_FIELD_KEYS.additionalAddressesOnly]}
            />
            <CheckboxInput
                id={EMAIL_DISTRIBUTION_FIELD_KEYS.projectOwnerOnly}
                name={EMAIL_DISTRIBUTION_FIELD_KEYS.projectOwnerOnly}
                label="Project Owner Only"
                description="If true, emails will only be sent to the administrator(s) of the project. Otherwise, all users assigned to the project will get an email."
                readOnly={readonly}
                onChange={FieldModelUtilities.handleChange(data, setData)}
                isChecked={FieldModelUtilities.getFieldModelBooleanValue(data, EMAIL_DISTRIBUTION_FIELD_KEYS.projectOwnerOnly)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_DISTRIBUTION_FIELD_KEYS.projectOwnerOnly)}
                errorValue={errors.fieldErrors[EMAIL_DISTRIBUTION_FIELD_KEYS.projectOwnerOnly]}
            />
            <DynamicSelectInput
                id={EMAIL_DISTRIBUTION_FIELD_KEYS.attachmentFormat}
                name={EMAIL_DISTRIBUTION_FIELD_KEYS.attachmentFormat}
                label="Attachment File Type"
                description="If a file type is selected, a file of that type, representing the message content, will be attached to the email."
                options={EMAIL_DISTRIBUTION_ATTACHMENT_OPTIONS}
                readOnly={readonly}
                clearable
                removeSelected
                onChange={FieldModelUtilities.handleChange(data, setData)}
                value={FieldModelUtilities.getFieldModelValues(data, EMAIL_DISTRIBUTION_FIELD_KEYS.attachmentFormat)}
                errorName={FieldModelUtilities.createFieldModelErrorKey(EMAIL_DISTRIBUTION_FIELD_KEYS.attachmentFormat)}
                errorValue={errors.fieldErrors[EMAIL_DISTRIBUTION_FIELD_KEYS.attachmentFormat]}
            />
        </>
    );
};
EmailDistributionConfiguration.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    createAdditionalEmailRequestBody: PropTypes.func.isRequired,
    data: PropTypes.object.isRequired,
    setData: PropTypes.func.isRequired,
    errors: PropTypes.object.isRequired,
    readonly: PropTypes.bool.isRequired
};

export default EmailDistributionConfiguration;
