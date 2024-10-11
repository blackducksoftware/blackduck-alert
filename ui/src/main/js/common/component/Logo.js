import React from 'react';
import '/src/main/css/logos.scss';
import BlackDuckLogo from '/src/main/img/BlackDuckLogo.png';

const Logo = () => (
    <div className="productLogo">
        <div className="logo">
            <div className="logoContainer">
                <img src={BlackDuckLogo} alt="logo" height="30px" />
                <span className="divider" />
                <span className="alertText">ALERT</span>
            </div>
        </div>
    </div>
);

export default Logo;
