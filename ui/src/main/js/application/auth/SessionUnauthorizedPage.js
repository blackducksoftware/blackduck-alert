import React from 'react';
import { useDispatch } from 'react-redux';
import Button from 'common/component/button/Button';
import { createUseStyles } from 'react-jss';
import { verifyLogin } from 'store/actions/session';
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

const SessionUnauthorizedPage = () => {
    const classes = useStyles();
    const dispatch = useDispatch();

    function handleLoginRedirect() {
        dispatch(verifyLogin());
    }

    return (
        <AuthorizationView>
            <div className={classes.dialogContainer}>
                <div>You have been logged out due to an inactive session.</div>
                <div>This site is only for registered users.</div>
                <Button id="unauthorized-login" onClick={handleLoginRedirect} text="Return to Login" buttonStyle="action" />
            </div>
        </AuthorizationView>
    );
};

export default SessionUnauthorizedPage;
