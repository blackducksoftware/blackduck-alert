import React from 'react';
import { createUseStyles } from 'react-jss';
import PasswordInput from 'common/component/input/PasswordInput';
import TextArea from 'common/component/input/TextArea';
import TextInput from 'common/component/input/TextInput';

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
    const classes = useStyles();
    
    return (
        <section className={classes.layout}>
            <header className={classes.certificateHeader}>Client Certificate</header>
            <div className={classes.certificateDescription}>Filling out the fields below will create a Mutual TLS certificate (MTLS). This MTLS will be used to provide valid authentication between your client and Alert.</div>
            <TextInput
                id="MTLSKeyContent"
                name="MTLSKeyContent"
                label="Key Content"
                description="Provide description for key content here."
                readOnly={false}
                onChange={() => { console.log('MTLSKeyContent Change')}}
                // value="MTLSKeyContent provide default value"
            />
            <PasswordInput
                id="MTLSKeyPassword"
                name="MTLSKeyPassword"
                label="Key Password"
                onChange={() => { console.log('MTLSKeyPassword change')}}
                // value={loginForm.password}
            />
            <TextArea
                id="MTLSCertificateContent"
                name="MTLSCertificateContent"
                label="Certificate Content"
                customDescription="Enter a valid Mutual TLS certificate (MTLS) below to provide authentication between your client and Alert."
                onChange={() => { console.log('MTLSCertificateContent change')}}
                // value="MTLSCertificateContent provide default value"
                // errorName="certificateContent"
                // errorValue={error.fieldErrors.certificateContent}
            />
        </section>
    );
};

export default MTLSCertificateLayout;
