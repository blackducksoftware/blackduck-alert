import React from 'react';
import { createUseStyles } from 'react-jss';
import Button from 'common/component/button/Button';

const useStyles = createUseStyles((theme) => ({
    samlLoginContainer: {
        minHeight: '75px'
    },
    separator: {
        padding: '10px 0',
        display: 'flex',
        alignItems: 'center',
        textAlign: 'center',
        color: theme.colors.grey.default,
        '&::before, &::after': {
            content: '""',
            flex: 1,
            borderBottom: `solid 1px ${theme.colors.grey.default}`
        },
        '&:not(:empty):before': {
            marginRight: '.5em',
            marginLeft: '3em'
        },
        '&:not(:empty):after': {
            marginLeft: '.5em',
            marginRight: '3em'
        }
    },
    samlLoginAction: {
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center'
    }
}));

const SamlLogin = () => {
    const classes = useStyles();

    // Should redirect to Okta
    function handleClick() {
        window.location.replace('/alert/saml2/authenticate/default');
    }

    return (
        <div className={classes.samlLoginContainer}>
            <div className={classes.separator}>Or</div>
            <div className={classes.samlLoginAction}>
                <Button onClick={handleClick} type="button" text="Login with SAML" />
            </div>
        </div>
    );
};

export default SamlLogin;
