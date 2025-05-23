import React, { useState } from 'react';
import { createUseStyles } from 'react-jss';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import EndpointSelectField from 'common/component/input/EndpointSelectField';
import { DISTRIBUTION_COMMON_FIELD_KEYS, DISTRIBUTION_URLS } from 'page/distribution/DistributionModel';
import { createReadRequest } from 'common/util/configurationRequestBuilder';
import * as PropTypes from 'prop-types';
import StatusMessage from 'common/component/StatusMessage';
import Modal from 'common/component/modal/Modal';

const useStyles = createUseStyles({
    configModalContent: {
        height: '100px'
    }
});

const BlackDuckSSOConfigImportModal = ({
    csrfToken, readOnly = false, initialSSOFieldData, updateSSOFieldData,
    isOpen, toggleModal, providerModel, setProviderModel
}) => {
    const classes = useStyles();
    const [loading, setLoading] = useState(false);
    const [errorMessage, setErrorMessage] = useState();

    const blackDuckConfigId = FieldModelUtilities.getFieldModelSingleValue(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId);

    const hideAndClearModal = () => {
        setErrorMessage(undefined);
        setLoading(false);
        toggleModal(false);
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
                        updateSSOFieldData({
                            ...initialSSOFieldData, enabled: data.ssoEnabled, metadataUrl: data.idpMetadataUrl || '', metadataMode: 'URL'
                        });
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

    function handleClose() {
        toggleModal(false);
    }

    function handleSubmit() {
        setLoading(true);
        retrieveBlackDuckSSOConfig();
    }

    return (
        <Modal
            isOpen={isOpen}
            size="md"
            title="Retrieve Black Duck SAML Configuration"
            closeModal={handleClose}
            handleCancel={handleClose}
            handleSubmit={handleSubmit}
            submitText="Import"
            showLoader={loading}
        >
            <div className={classes.configModalContent}>
                <EndpointSelectField
                    id={DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId}
                    csrfToken={csrfToken}
                    endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                    fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId}
                    label="Black Duck Configuration"
                    description="The Black Duck configuration from which to retrieve the SAML configuration."
                    clearable={false}
                    readOnly={readOnly} 
                    required
                    createRequestBody={() => providerModel}
                    onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                    value={FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId)}
                    errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId)}
                />
                <StatusMessage
                    id="import-status-message"
                    errorMessage={errorMessage}
                    errorIsDetailed={false}
                />
            </div>
        </Modal>
    );
};

BlackDuckSSOConfigImportModal.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    readOnly: PropTypes.bool,
    initialSSOFieldData: PropTypes.object.isRequired,
    isOpen: PropTypes.bool.isRequired,
    providerModel: PropTypes.object.isRequired,
    setProviderModel: PropTypes.func.isRequired,
    toggleModal: PropTypes.func.isRequired,
    updateSSOFieldData: PropTypes.func.isRequired
};

export default BlackDuckSSOConfigImportModal;
