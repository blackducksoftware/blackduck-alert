import React from 'react';
import * as PropTypes from 'prop-types';
import { createUseStyles } from 'react-jss';
import HeaderUtilities from 'common/util/HeaderUtilities';

const useStyles = createUseStyles({
    samlLoginContainer: {
        minHeight: '75px'
    },
    separator: {
        padding: '10px 0',
        display: 'flex',
        alignItems: 'center',
        textAlign: 'center',
        color: 'grey',
        '&::before, &::after': {
            content: '""',
            flex: 1,
            borderBottom: 'solid 1px grey'
        },
        '&:not(:empty):before': {
            marginRight: '.5em',
            marginLeft: '3em'
        },
        '&:not(:empty):after': {
            marginLeft: '.5em',
            marginRight: '3em'
        },
    },
    samlLoginAction: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center'
    },
    loginButton: {
        background: 'none',
        width: '200px',
        color: 'inherit',
        border: 'solid .5px',
        padding: ['6px', '20px'],
        margin: ['10px', 'auto', '20px', 'auto'],
        font: 'inherit',
        cursor: 'pointer',
        borderRadius: '4px',
        fontSize: '14px',
        backgroundColor: '#2E3B4E',
        color: 'white',
        '&:focus': {
            outline: 0
        }
    }
})

const SamlLogin = () => {
    const classes = useStyles();

    // Should redirect to Okta
    function authenticateSAML() {
        const headersUtil = new HeaderUtilities();

        const request = fetch('/alert/saml2/authenticate/default', {
            credentials: 'same-origin',
            headers: headersUtil.getHeaders()
        });

        request.then((response) => {
            console.log('RESPONSE', response.json());
        })
    }

    return (
        <div className={classes.samlLoginContainer}>
            <div className={classes.separator}>Or</div>
            <div className={classes.samlLoginAction}>
                <button className={classes.loginButton} type="button" onClick={() => authenticateSAML()}>
                    Login with SAML
                </button>
            </div>
        </div>
    );
};

// AzureBoardsForm.propTypes = {
//     csrfToken: PropTypes.string.isRequired,
//     errorHandler: PropTypes.object.isRequired,
//     readonly: PropTypes.bool,
//     displayTest: PropTypes.bool,
// };

export default SamlLogin;
