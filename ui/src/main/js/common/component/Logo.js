import React from 'react';
import '/src/main/css/logos.scss';

const Logo = () => (
    <div className="productLogo">
        <div className="logo">
            <div className="logoContainer">
                <img src="https://www.blackduck.com/content/dam/black-duck/en-us/images/BlackDuckLogo-OnDark.svg" alt="logo" height="30px" />
                <span className="divider" />
                <span className="alertText">ALERT</span>
            </div>
        </div>
    </div>
);

export default Logo;
