import React from 'react';
import Logo from 'common/component/Logo';

import '../../../css/header.scss';

const Header = () => (
    <div className="header">
        <Logo />
        <div className="submitContainers">
            <div className="loginSpacer" />
        </div>
    </div>
);

export default Header;
