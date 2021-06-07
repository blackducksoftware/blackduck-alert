import React, {
    useEffect,
    useState
} from 'react';
import * as FieldModelUtilities from 'common/util/fieldModelUtilities';
import PopUp from "common/PopUp";
import EndpointSelectField from "common/input/EndpointSelectField";
import {
    DISTRIBUTION_COMMON_FIELD_KEYS,
    DISTRIBUTION_URLS
} from "page/distribution/DistributionModel";
import { CONTEXT_TYPE } from "common/util/descriptorUtilities";
import { BLACKDUCK_INFO } from "page/provider/blackduck/BlackDuckModel";
import { createReadRequest } from "common/util/configurationRequestBuilder";
import { AUTHENTICATION_SAML_FIELD_KEYS } from "./AuthenticationModel";
import * as HttpErrorUtilities from "../../common/util/httpErrorUtilities";
import * as PropTypes from "prop-types";

const BlackDuckSSOConfigImportModal = ({ csrfToken, readOnly, label, show, onHide, currentSamlFields, updateSamlFields }) => {
    const [loading, setLoading] = useState(false);
    const [blackDuckSSOConfig, setBlackDuckSSOConfig] = useState({});
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [providerModel, setProviderModel] = useState(FieldModelUtilities.createEmptyFieldModel([], CONTEXT_TYPE.DISTRIBUTION, BLACKDUCK_INFO.key));

    useEffect(() => {
        if (blackDuckSSOConfig.ssoEnabled) {
            const newKeyToValues = {};
            newKeyToValues[AUTHENTICATION_SAML_FIELD_KEYS.enabled] = blackDuckSSOConfig.ssoEnabled || currentSamlFields.keyToValues[AUTHENTICATION_SAML_FIELD_KEYS.enabled];
            newKeyToValues[AUTHENTICATION_SAML_FIELD_KEYS.entityId] = blackDuckSSOConfig.spEntityId || currentSamlFields.keyToValues[AUTHENTICATION_SAML_FIELD_KEYS.entityId];
            newKeyToValues[AUTHENTICATION_SAML_FIELD_KEYS.entityBaseUrl] = blackDuckSSOConfig.idpMetadataUrl || currentSamlFields.keyToValues[AUTHENTICATION_SAML_FIELD_KEYS.entityBaseUrl];
            newKeyToValues[AUTHENTICATION_SAML_FIELD_KEYS.metadataUrl] = blackDuckSSOConfig.samlMetadataUrl || currentSamlFields.keyToValues[AUTHENTICATION_SAML_FIELD_KEYS.metadataUrl];
            // blackDuckSSOConfig.idpMetadataFileUploaded;
            // blackDuckSSOConfig.groupSynchronizationEnabled;
            // blackDuckSSOConfig.localLogoutEnabled;
            // blackDuckSSOConfig.spExternalUrl;
            // blackDuckSSOConfig.userCreationEnabled;
            const combinedFieldModels = FieldModelUtilities.combineFieldModels(currentSamlFields, { keyToValues: newKeyToValues });
            // updateSamlFields(combinedFieldModels);
        }
        setLoading(false);
    }, [blackDuckSSOConfig]);

    const blackDuckConfigId = FieldModelUtilities.getFieldModelValues(providerModel, DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId);

    const retrieveBlackDuckSSOConfig = () => {
        const request = createReadRequest(`/alert/api/blackduck/${blackDuckConfigId}/sso/configuration`, csrfToken);
        request.then((response) => {
            if (response.ok) {
                response.json().then((data) => {
                    setBlackDuckSSOConfig(data);
                });
            } else {
                response.json()
                    .then((data) => {
                        setBlackDuckSSOConfig({});
                        console.log(`Error: {data}`)
                        setErrors(data);
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
            cancelLabel={"Done"}
            onCancel={onHide}
            includeSave={false}
            handleTest={() => {
                setLoading(true);
                retrieveBlackDuckSSOConfig();
            }}
            testLabel={"Import"}
            includeTest={true}
            show={show}
            title={label}
            performingAction={loading}
        >
            <EndpointSelectField
                id={DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId}
                csrfToken={csrfToken}
                endpoint={DISTRIBUTION_URLS.endpointSelectPath}
                fieldKey={DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId}
                label="BlackDuck Configuration"
                description="The provider configuration from which to import."
                clearable={false}
                readOnly={readOnly}
                required
                createRequestBody={() => providerModel}
                onChange={FieldModelUtilities.handleChange(providerModel, setProviderModel)}
                value={blackDuckConfigId}
                errorName={FieldModelUtilities.createFieldModelErrorKey(DISTRIBUTION_COMMON_FIELD_KEYS.providerConfigId)}
                errorValue={undefined}
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
    currentSamlFields: PropTypes.object.isRequired,
    updateSamlFields: PropTypes.func.isRequired
};

BlackDuckSSOConfigImportModal.defaultProps = {
    readOnly: false,
    label: "BlackDuck SSO Config Import",
    show: false
};

export default BlackDuckSSOConfigImportModal;
