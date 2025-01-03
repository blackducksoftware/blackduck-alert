import React, { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { createUseStyles } from 'react-jss';
import PasswordInput from 'common/component/input/PasswordInput';
import TextInput from 'common/component/input/TextInput';
import Header from 'common/component/Header';
import Button from 'common/component/button/Button';
import { login } from 'store/actions/session';
import SamlLogin from 'application/auth/SamlLogin';

const useStyles = createUseStyles({
    loginButton: {
        display: 'flex',
        justifyContent: 'flex-end',
        padding: '6.5px',
        marginRight: '2.25em'
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
        <div className="wrapper">
            <div className="loginContainer">
                <div className="loginBox">
                    <Header />
                    <form method="POST" className="form-horizontal loginForm" onSubmit={handleSubmit}>
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
                            autoFocus
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
                            <Button id="loginSubmit" text="Login" type="submit" showLoader={fetching} />
                        </div>
                    </form>
                    {samlEnabled && (
                        <SamlLogin />
                    )}
                </div>
            </div>
        </div>
    );
};

export default LoginPage;
