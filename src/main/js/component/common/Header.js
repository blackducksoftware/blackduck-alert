import React, { Component } from 'react';
import Logo from 'component/common/Logo';

import '../../../css/header.scss';

class Header extends Component {
    render() {
        return (
            <div className="header">
                <Logo />
                <div className="submitContainers">
                    <div className="loginSpacer" />
                </div>
            </div>
        );
    }
}

export default Header;
