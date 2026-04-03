import React from 'react';
import { createUseStyles } from 'react-jss';
import { useDispatch } from 'react-redux';
import { verifyLogin } from 'store/actions/session';
import Button from 'common/component/button/Button';
import AuthorizationView from 'common/component/AuthorizationView';

const useStyles = createUseStyles({
    dialogContainer: {
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        padding: '2.25em',
        rowGap: '1.5em'
    }
});

const LogoutPage = () => {
    const classes = useStyles();
    const dispatch = useDispatch();
    function handleLoginRedirect() {
        dispatch(verifyLogin());
    }

    return (
        <AuthorizationView>
            <div className={classes.dialogContainer}>
                <div>You've successfully logged out of Alert!</div>
                <div>To complete logout please close your browser or click below.</div>
                <Button id="logout-redirect-login" onClick={handleLoginRedirect} text="Return to Login" buttonStyle="action" />
            </div>
        </AuthorizationView>
    );
};

export default LogoutPage;