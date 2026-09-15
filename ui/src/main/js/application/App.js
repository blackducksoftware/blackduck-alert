import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';

import MainPage from 'application/MainPage';
import LoginPage from 'application/auth/LoginPage';
import Footer from 'application/Footer';
import { verifyLogin, verifySaml } from 'store/actions/session';
import { getAboutInfo } from 'store/actions/about';
import * as IconUtility from 'common/util/iconUtility';
import LogoutPage from 'application/auth/LogoutPage';
import SessionUnauthorizedPage from 'application/auth/SessionUnauthorizedPage';

import 'bootstrap/dist/css/bootstrap.css';
import '../../css/main.scss';
import { BrowserRouter } from 'react-router-dom';

IconUtility.loadIconData();

const App = () => {
    const dispatch = useDispatch();
    const { loggedIn, logoutPerformed, sessionUnauthorizationPerformed, initializing } = useSelector((state) => state.session);

    useEffect(() => {
        dispatch(verifyLogin());
        dispatch(verifySaml());
        dispatch(getAboutInfo());
    }, []);

    if (initializing) {
        return (<div />);
    }

    if (logoutPerformed) {
        return <LogoutPage />;
    }

    if (sessionUnauthorizationPerformed) {
        return <SessionUnauthorizedPage />;
    }

    const contentPage = loggedIn ? <MainPage /> : <LoginPage />;

    return (
        <BrowserRouter>
            <div style={{ height: '96.5vh' }}>
                {contentPage}
            </div>
            <div style={{ height: '3.5vh' }}>
                <Footer />
            </div>
        </BrowserRouter>
    );
};

export default App;
