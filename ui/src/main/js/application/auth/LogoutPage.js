import React from 'react';
import { createUseStyles } from 'react-jss';
import { useDispatch } from 'react-redux';
import { verifyLogin } from 'store/actions/session';
import Header from 'common/component/Header';
import Button from 'common/component/button/Button';

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
        <div className="wrapper">
            <div className="loginContainer">
                <div className="loginBox">
                    <Header />
                    <div className={classes.dialogContainer}>
                        <div>You've successfully logged out of Alert!</div>
                        <div>To complete logout please close your browser or click below.</div>
                        <Button id="logout-redirect-login" onClick={handleLoginRedirect} text="Return to Login" />
                    </div>
                </div>
            </div>
        </div>
    );
};

export default LogoutPage;