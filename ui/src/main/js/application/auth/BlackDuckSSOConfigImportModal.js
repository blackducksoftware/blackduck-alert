import React, { useState } from 'react';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import PopUp from 'common/PopUp';
import EndpointSelectField from 'common/input/EndpointSelectField';
import { DISTRIBUTION_COMMON_FIELD_KEYS, DISTRIBUTION_URLS } from 'page/distribution/DistributionModel';
import { CONTEXT_TYPE } from 'common/util/descriptorUtilities';
import { BLACKDUCK_INFO } from 'page/provider/blackduck/BlackDuckModel';
import { createReadRequest } from 'common/util/configurationRequestBuilder';
import * as PropTypes from 'prop-types';
import StatusMessage from 'common/StatusMessage';
import { AUTHENTICATION_SAML_FIELD_KEYS } from './AuthenticationModel';

const BlackDuckSSOConfigImportModal = ({
    csrfToken, readOnly, label, show, onHide, initialSSOFieldData, updateSSOFieldData
}) => {
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState();
    const [providerModel, setProviderModel] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, BLACKDUCK_INFO.key));

    const blackDuckConfigId = FieldModelUtilities.getFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId);

    const hideAndClearModal = () => {
        setErrorMessage(undefined);
        setLoading(false);
        onHide();
    };

    const retrieveBlackDuckSSOConfig = () => {
        if (!blackDuckConfigId) {
            setErrorMessage('Please choose a Black Duck configuration from which to import.');
            setLoading(false);
            return;
        }

        const request = createReadRequest(`/alert/api/blackduck/${blackDuckConfigId}/sso/configuration`, csrfToken);
        request.then((response) => {
            if (response.ok) {
                response.json().then((data) => {
                    if (data) {
                        let updatedFieldData = FieldModelUtilities.updateFieldModelSingleValue(initialSSOFieldData, AUTHENTICATION_SAML_FIELD_KEYS.enabled, data.ssoEnabled);
                        updatedFieldData = FieldModelUtilities.updateFieldModelSingleValue(updatedFieldData, AUTHENTICATION_SAML_FIELD_KEYS.metadataUrl, data.idpMetadataUrl || '');
                        updateSSOFieldData(updatedFieldData);
                    }
                    hideAndClearModal();
                });
            } else {
                response.json()
                    .then((data) => {
                        setErrorMessage(data.message);
                        setLoading(false);
                    });
            }
        });
    };

    return (
        <PopUp
            id={label}
            onKeyDown={(e) => e.stopPropagation()}
            onClick={(e) => e.stopPropagation()}
            onFocus={(e) => e.stopPropagation()}
            onMouseOver={(e) => e.stopPropagation()}
            onCancel={hideAndClearModal}
            includeSave={false}
            handleTest={() => {
                setLoading(true);
                retrieveBlackDuckSSOConfig();
            }}
            testLabel="Import"
            includeTest
            show={show}
            title={label}
            performingAction={loading}
        >
            <EndpointSelectField
                id={DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId}
                csrfToken={csrfToken}
                endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId}
                label="Black Duck Configuration"
                description="The Black Duck configuration from which to retrieve the SAML configuration. Does not import Entity ID as that will usually not match the Entity ID used for Alert."
                clearable={false}
                readOnly={readOnly}
                required
                createRequestBody={() => providerModel}
                onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                value={blackDuckConfigId}
                errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId)}
            />
            <StatusMessage
                id="import-status-message"
                errorMessage={errorMessage}
                errorIsDetailed={false}
            />
        </PopUp>
    );
};

BlackDuckSSOConfigImportModal.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    readOnly: PropTypes.bool,
    label: PropTypes.string,
    show: PropTypes.bool,
    onHide: PropTypes.func.isRequired,
    initialSSOFieldData: PropTypes.object.isRequired,
    updateSSOFieldData: PropTypes.func.isRequired
};

BlackDuckSSOConfigImportModal.defaultProps = {
    readOnly: false,
    label: 'BlackDuck SSO Config Import',
    show: false
};

export default BlackDuckSSOConfigImportModal;
