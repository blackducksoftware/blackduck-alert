import React, { useEffect, useState } from 'react';
import { createUseStyles } from 'react-jss';
import PasswordInput from 'common/component/input/PasswordInput';
import TextArea from 'common/component/input/TextArea';
import ConcreteConfigurationForm from 'common/configuration/global/concrete/ConcreteConfigurationForm';
import * as HttpErrorUtilities from 'common/util/httpErrorUtilities';
import { useDispatch, useSelector } from 'react-redux';
import { fetchClientCertificate, postClientCertificate, deleteClientCertificate } from 'store/actions/client-certificate';

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

const MTLSCertificateLayout = () => {
    const dispatch = useDispatch();
    const classes = useStyles();
    const [errors, setErrors] = useState(HttpErrorUtilities.createEmptyErrorObject());
    const [formData, setFormData] = useState({});
    const { data, error } = useSelector((state) => state.clientCertificate);

    useEffect(() => {
        setFormData(data);
    }, [data]);

    function fetchCertificateData() {
        dispatch(fetchClientCertificate());
    };

    function postData() {
        dispatch(postClientCertificate(formData));
    }

    function deleteData() {
        dispatch(deleteClientCertificate());
    }

    function getDisplayValue(field) {
        if (formData) {
            return formData[field];
        }
        return '';
    }

    const handleOnChange = (label) => (
        ({ target: { value } }) => {
            setFormData((clientCertificateData) => ({ ...clientCertificateData, [label]: value }));
        }
    );

    return (
        <section className={classes.layout}>
            <header className={classes.certificateHeader}>Client Certificate</header>
            <div className={classes.certificateDescription}>Filling out the fields below will create a Mutual TLS certificate (MTLS). This certificate will be used to provide valid authentication between your client and Alert.</div>
            
            <ConcreteConfigurationForm
                formDataId="MTLSFormID"
                setErrors={(formErrors) => setErrors(formErrors)}
                getRequest={fetchCertificateData}
                updateRequest={postData}
                deleteRequest={deleteData}
                ignoreValidation={true}
                displayTest={false}
                errorHandler={() => console.log('errorHandler')}
                deleteLabel="Reset"
                submitLabel="Submit"
            > 
                <PasswordInput
                    id="keyPassword"
                    name="keyPassword"
                    label="Key Password"
                    description="Provide description for key content here."
                    onChange={handleOnChange('keyPassword')}
                    value={getDisplayValue('keyPassword')}
                    errorName="keyPassword"
                    errorValue={error.fieldErrors.keyPassword}
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
                    errorValue={error.fieldErrors.keyContent}
                    sizeClass="col-sm-8 flex-column p-2"
                />
                <TextArea
                    id="clientCertificateContent"
                    name="clientCertificateContent"
                    label="Certificate Content"
                    description="Enter a valid Mutual TLS certificate (MTLS) below to provide authentication between your client and Alert."
                    onChange={handleOnChange('clientCertificateContent')}
                    value={getDisplayValue('clientCertificateContent')}
                    errorName="clientCertificateContent"
                    errorValue={error.fieldErrors.clientCertificateContent}
                    sizeClass="col-sm-8 flex-column p-2"
                />                
            </ConcreteConfigurationForm>
        </section>
    );
};

export default MTLSCertificateLayout;
