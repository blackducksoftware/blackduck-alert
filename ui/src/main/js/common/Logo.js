import React from 'react';

import '../../css/logos.scss';

const Logo = () => (
    <div className="productLogo">
        <span className="synopsysLogoSpan">
            <img
                className="synopsysHeaderLogo"
                src="https://www.synopsys.com/content/dam/synopsys/company/about/legal/synopsys-logos/whitelogo/synopsys_wht.png"
                alt="Synopsys"
            />
            <span className="headerStandardSize">
                <span className="synopsysHeaderLogoVerticalBarSpace">|</span>
                ALERT
            </span>
        </span>
    </div>
);

export default Logo;
