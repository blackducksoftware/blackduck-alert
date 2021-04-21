import React from 'react';
import Header from 'component/common/Header';

function LogoutPage() {
    return (
        <div className="wrapper">
            <div className="loginContainer">
                <div className="loginBox">
                    <Header />
                    <div className="form-horizontal loginForm">
                        <div className="row">
                            <div className="col-sm-12 text-center">
                                <div className="d-inline-flex flex-column p-2">You've successfully logged out of Alert!</div>
                                <div className="d-inline-flex flex-column p-2">Please close your browser to complete logout.</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default LogoutPage;
