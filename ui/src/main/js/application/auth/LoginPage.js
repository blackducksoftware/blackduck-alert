import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';
import Button from 'common/component/button/Button';
import { login } from 'store/actions/session';
import SamlLogin from 'application/auth/SamlLogin';
import AuthorizationView from 'common/component/AuthorizationView';

const useStyles = createUseStyles({
    loginForm: {
        display: 'flex',
        flexDirection: 'column',
        padding: ['20px', '40px']
    },
    loginButton: {
        display: 'flex',
        justifyContent: 'flex-end',
        paddingTop: '10px'
    }
});

const LoginPage = () => {
    const dispatch = useDispatch();
    const classes = useStyles();
    const { errorMessage, fetching, samlEnabled } = useSelector((state) => state.session);
    const [loginForm, setLoginForm] = useState({
        username: '',
        password: ''
    });

    function handleChange({ target }) {
        const { name, value } = target;
        setLoginForm({ ...loginForm, [name]: value });
    }

    function handleSubmit(evt) {
        evt.preventDefault();
        dispatch(login(loginForm.username, loginForm.password));
    }

    return (
        <AuthorizationView>
            <form method="POST" className={classes.loginForm} onSubmit={handleSubmit}>
                { errorMessage && (
                    <div className="alert alert-danger">
                        <p name="configurationMessage">{errorMessage}</p>
                    </div>
                )}

                <TextInput
                    id="loginUsername"
                    label="Username"
                    name="username"
                    onChange={handleChange}
                    value={loginForm.username}
                    readOnly={false}
                />

                <PasswordInput
                    id="loginPassword"
                    label="Password"
                    name="password"
                    onChange={handleChange}
                    value={loginForm.password}
                />
                <div className={classes.loginButton}>
                    <Button id="loginSubmit" text="Login" type="submit" showLoader={fetching} buttonStyle="action" />
                </div>
            </form>
            {samlEnabled && (
                <SamlLogin />
            )}
        </AuthorizationView>
    );
};

export default LoginPage;
