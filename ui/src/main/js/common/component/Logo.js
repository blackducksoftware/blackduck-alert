import React from 'react';

import '../../../css/logos.scss';
import BlackDuckLogo from '../../../resources/BlackDuckLogo.png';

const Logo = () => (
    <div className="productLogo">
        <span className="blackduckLogoSpan">
            <img
                className="blackduckHeaderLogo"
                src={BlackDuckLogo}
                alt="Black Duck"
            />
            <span className="headerStandardSize">
                <span className="blackduckHeaderLogoVerticalBarSpace">|</span>
                ALERT
            </span>
        </span>
    </div>
);

export default Logo;
