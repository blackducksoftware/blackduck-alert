import React from 'react';
import { useDispatch } from 'react-redux';
import { createUseStyles } from 'react-jss';

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
    const dispatch = useDispatch();

    // Should redirect to Okta
    function handleClick() {
        window.location.replace('/alert/saml2/authenticate/default');
    }

    return (
        <div className={classes.samlLoginContainer}>
            <div className={classes.separator}>Or</div>
            <div className={classes.samlLoginAction}>
                <button className={classes.loginButton} type="button" onClick={handleClick}>
                    Login with SAML
                </button>
            </div>
        </div>
    );
};

export default SamlLogin;
