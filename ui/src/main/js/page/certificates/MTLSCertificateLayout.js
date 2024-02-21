import React, { useState } from 'react';
import { createUseStyles } from 'react-jss';
import { createEmptyErrorObject } from 'common/util/httpErrorUtilities';
import { 
    CLIENT_CERTIFICATE_URL,
    createDeleteRequest,
    createNewConfigurationRequest,
    createReadRequest
} from 'common/util/configurationRequestBuilder';

import PasswordInput from 'common/component/input/PasswordInput';
import TextArea from 'common/component/input/TextArea';
import ConcreteConfigurationForm from 'common/configuration/global/concrete/ConcreteConfigurationForm';

const useStyles = createUseStyles({
    layout: {
        margin: '20px 20px 0 20px'
    },
    certificateHeader: {
        fontSize: '17px',
        borderBottom: 'solid 1px #dddddd',
        marginBottom: '16px',
        marginTop: '4px',
        paddingBottom: '4px'
    },
    certificateDescription: {
        marginBottom: '20px'
    }
})

const MTLSCertificateLayout = ({ csrfToken , errorHandler }) => {
    const classes = useStyles();
    const [errors, setErrors] = useState(createEmptyErrorObject());
    const [formData, setFormData] = useState({});
    const [isDeleteDisabled, setIsDeleteDisabled] = useState(true);
    const [isSaveDisabled, setIsSaveDisabled] = useState(false);

    function getDisplayValue(field) {
        if (formData) {
            return formData[field];
        }
        return '';
    }

    const handleOnChange = (label) => (
        ({ target: { value } }) => {
            setFormData((clientCertificateData) => {
                if (value.trim() === '') {
                    const { [label]: removeKey, ...data } = clientCertificateData;
                    return data;
                }

                return { ...clientCertificateData, [label]: value }
            });
        }
    );

    const fetchData = async () => {
        const response = await createReadRequest(CLIENT_CERTIFICATE_URL, csrfToken);
        if (response.ok) {
            setFormData({
                keyPassword: '***********',
                keyContent: '***********',
                certificateContent: '***********'
            });
            setIsDeleteDisabled(false);
            setIsSaveDisabled(true);
        }
    };

    const formHasData = (formData.hasOwnProperty('keyPassword') || formData.hasOwnProperty('keyContent') || formData.hasOwnProperty('certificateContent'));

    function onSaveSuccess() {
        setIsSaveDisabled(true);
    }

    function onDeleteSuccess() {
        // We have to manually set form data back to an empty object since a GET request will return a 404.  If the GET request returned an empty object and not a 404
        // this onDeleteSuccess could be reduced to `fetchData()`
        setFormData({});
        setIsDeleteDisabled(true);
        setIsSaveDisabled(false);
    }

    return (
        <section className={classes.layout}>
            <header className={classes.certificateHeader}>Client Certificate</header>
            <div className={classes.certificateDescription}>Filling out the fields below will create a Mutual TLS certificate (MTLS). This certificate will be used to provide valid authentication between your client and Alert.</div>
            
            <ConcreteConfigurationForm
                formDataId="MTLSFormID"
                setErrors={(formErrors) => setErrors(formErrors)}
                getRequest={fetchData}
                updateRequest={() => createNewConfigurationRequest(CLIENT_CERTIFICATE_URL, csrfToken, formData)}
                deleteRequest={() => createDeleteRequest(CLIENT_CERTIFICATE_URL, csrfToken)}
                afterSuccessfulSave={onSaveSuccess}
                postDeleteAction={onDeleteSuccess}
                ignoreValidation={true}
                displayTest={false}
                errorHandler={errorHandler}
                deleteLabel="Delete"
                submitLabel="Save"
                isSaveDisabled={!formHasData || isSaveDisabled}
                isDeleteDisabled={isDeleteDisabled}
            > 
                <PasswordInput
                    id="keyPassword"
                    name="keyPassword"
                    label="Key Password"
                    description="Provide description for key content here."
                    onChange={handleOnChange('keyPassword')}
                    value={getDisplayValue('keyPassword')}
                    errorName="keyPassword"
                    errorValue={errors.fieldErrors.keyPassword}
                    isDisabled={!isDeleteDisabled}
                />
                <TextArea
                    id="keyContent"
                    name="keyContent"
                    label="Key Content"
                    description="Provide description for key content here."
                    readOnly={false}
                    onChange={handleOnChange('keyContent')}
                    value={getDisplayValue('keyContent')}
                    errorName="keyContent"
                    errorValue={errors.fieldErrors.keyContent}
                    sizeClass="col-sm-8 flex-column p-2"
                    isDisabled={!isDeleteDisabled}
                />
                <TextArea
                    id="certificateContent"
                    name="certificateContent"
                    label="Certificate Content"
                    description="Enter a valid Mutual TLS certificate (MTLS) below to provide authentication between your client and Alert."
                    onChange={handleOnChange('certificateContent')}
                    value={getDisplayValue('certificateContent')}
                    errorName="certificateContent"
                    errorValue={errors.fieldErrors.certificateContent}
                    sizeClass="col-sm-8 flex-column p-2"
                    isDisabled={!isDeleteDisabled}
                />                
            </ConcreteConfigurationForm>
        </section>
    );
};

MTLSCertificateLayout.propTypes = {
    csrfToken: PropTypes.string.isRequired,
    errorHandler: PropTypes.object.isRequired
}

export default MTLSCertificateLayout;
